package net.jahhan.init.initer;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.cache.Redis;
import net.jahhan.cache.constants.RedisConnectType;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.BaseVariable;
import net.jahhan.common.extension.context.VariableContext;
import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.jdbc.constant.JDBCConstants;
import net.jahhan.jdbc.context.DBVariable;
import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
import net.jahhan.jdbc.globaltransaction.DBConnExecutorHolderCache;
import net.jahhan.jdbc.utils.DBConnExecutorHolderUtil;
import net.jahhan.jedis.JedisSentinelPoolExt;
import net.jahhan.lock.communication.LockWaitingThreadHolder;
import net.jahhan.spi.common.BroadcastSender;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

@InitAnnocation(initOverWait = false)
@Slf4j
public class GlobalLockLisenterIniter extends JedisPubSub implements BootstrapInit {

	private final String database = RedisConstants.GLOBAL_LOCK;

	private final String MAXACTIVE = "1000"; // 一个pool的最大并发数

	private final String MAXIDLE = "10"; // pool中最大的空闲数量

	private final String MAXWAIT = "30000";// 获取一个jedis实例的超时时间，如果超过这个时间，就会抛出异常，单位ms

	private final int TIMEOUT = 0;// 用于设置socket的timeout，单位是ms

	@Inject
	private BroadcastSender broadcastSender;
	@Inject
	private DBConnExecutorHolderUtil dBConnExecutorHolderUtil;

	@SuppressWarnings("static-access")
	@Override
	public void execute() {
		Boolean inUse = RedisConstants.isInUse();
		if (!inUse) {
			return;
		}
		Properties is = PropertiesUtil.getProperties("redis");
		String host = is.getProperty(database + ".host");
		GenericObjectPoolConfig config = new JedisPoolConfig();
		// 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
		// 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
		config.setMaxTotal(Integer.parseInt(is.getProperty(database + ".maxActive", MAXACTIVE)));
		config.setMaxIdle(Integer.parseInt(is.getProperty(database + ".maxIdle", MAXIDLE)));
		config.setMaxWaitMillis(Long.parseLong(is.getProperty(database + ".maxWait", MAXWAIT)));
		// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
		String password = is.getProperty(database + ".password", null);
		// config.setTestOnBorrow(configurationManager.getRedisTestOnBorrow());
		String type = is.getProperty(database + ".type", RedisConnectType.Sentinel.toString());
		RedisConnectType redisConnectType = RedisConnectType.valueOf(type);
		Pool<Jedis> pool = null;
		switch (redisConnectType) {
		case Sentinel: {
			String masterName = is.getProperty(database + ".masterName", "mymaster");
			pool = new JedisSentinelPoolExt(masterName, host, config, password, TIMEOUT, 5, true);
			break;
		}
		case Simple: {
			int port = Integer.parseInt(is.getProperty(database + ".port"));
			int redisDatabase = Integer.parseInt(is.getProperty(database + ".database", "0"));
			pool = new JedisPool(config, host, port, TIMEOUT, password, redisDatabase);
			break;
		}
		default:
			break;
		}
		Redis jedis = new Redis(pool, redisConnectType);
		while (true) {
			try {
				Jedis writeJedis = jedis.getTemplate().getWriteJedis();
				Client client = writeJedis.getClient();
				this.proceedWithPatterns(client, "__key*__:*");
				Thread.currentThread().sleep(3000);
				client.close();
			} catch (JedisConnectionException | InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		String[] split = message.split(":");
		log.debug("收到lock消息：" + message);
		String messageType = split[0];
		if (messageType.equals("WEAKUP_NOTIFY")) {
			String lockAndChainId = split[1];
			String[] split2 = lockAndChainId.split("\\|");
			String lock = split2[0];
			String chainId = split2[1];
			LockWaitingThreadHolder.weakUp(chainId, lock);
		}
		if (messageType.equals("WEAKUP_TIMEOUT_NOTIFY")) {
			String lockAndChainId = split[1];
			String[] split2 = lockAndChainId.split("\\|");
			String lock = split2[0];
			String chainId = split2[1];
			LockWaitingThreadHolder.compete(lock, chainId);
		}
		if (messageType.equals("LOCK_TIMEOUT_NOTIFY")) {
			String lockAndChainId = split[1];
			String[] split2 = lockAndChainId.split("\\|");
			String lock = split2[0];
			String chainId = split2[1];
			LockWaitingThreadHolder.compete(lock, chainId);
		}
		if (messageType.equals("TRANSACTION_COMMIT")) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String chainId = split[1];
						BaseContext applicationContext = BaseContext.CTX;
						VariableContext variableContext = new VariableContext();
						if (null == applicationContext.getThreadLocalUtil().getValue()) {
							applicationContext.getThreadLocalUtil().openThreadLocal(variableContext);
						}
						DBVariable dbVariable = DBVariable.getDBVariable();
						long holdTimeOut = JDBCConstants.getHoldTimeOut();
						Map<String, List<DBConnExecutorHolder>> dbExecutorHolders = DBConnExecutorHolderCache
								.getDbExecutorHolders(chainId);
						long startTime = System.currentTimeMillis();
						if (null != dbExecutorHolders) {

							Set<String> dataSourceSet = dbExecutorHolders.keySet();

							for (String dataSource : dataSourceSet) {
								dbVariable.initConnectionData(dataSource);
								List<DBConnExecutorHolder> dBConnExecutorHolderlist = dbExecutorHolders.get(dataSource);
								if (null != dBConnExecutorHolderlist) {
									for (DBConnExecutorHolder dbConnExecutorHolder : dBConnExecutorHolderlist) {
										dbVariable.addDBConnExecutorHolder(dataSource, dbConnExecutorHolder);
										if (dbConnExecutorHolder.getStartTime() < startTime) {
											startTime = dbConnExecutorHolder.getStartTime();
										}
									}
								}
							}
						}

						Set<String> dataSources = dbVariable.getDataSources();
						if (null != dataSources && dataSources.size() > 0) {
							long waitTime = holdTimeOut * 1000 - (System.currentTimeMillis() - startTime);
							if (waitTime > 0) {
								dBConnExecutorHolderUtil.commit(true);
							} else {
								broadcastSender.send("TRANSACTION_ROLLBACK",
										BaseVariable.getBaseVariable().getChainId());
								dBConnExecutorHolderUtil.commit(false);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			t.start();
		}
		if (messageType.equals("TRANSACTION_ROLLBACK")) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String chainId = split[1];
						BaseContext applicationContext = BaseContext.CTX;
						VariableContext variableContext = new VariableContext();
						if (null == applicationContext.getThreadLocalUtil().getValue()) {
							applicationContext.getThreadLocalUtil().openThreadLocal(variableContext);
						}
						DBConnExecutorHolderCache.initDBVariable(chainId);
						dBConnExecutorHolderUtil.commit(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			t.start();
		}
	}

}
