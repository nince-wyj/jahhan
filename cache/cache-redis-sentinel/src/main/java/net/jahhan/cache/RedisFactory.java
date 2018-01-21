package net.jahhan.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.cache.constants.RedisConnectType;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.jedis.JedisSentinelPoolExt;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

/*
 * 使用前需要调用init方法
 */
@Slf4j
public class RedisFactory {

	private final static String MAXACTIVE = "1000"; // 一个pool的最大并发数

	private final static String MAXIDLE = "10"; // pool中最大的空闲数量

	private final static String MAXWAIT = "60000";// 获取一个jedis实例的超时时间，如果超过这个时间，就会抛出异常，单位ms

	private final static String TIMEOUT = "60000";// 用于设置socket的timeout，单位是ms

	public static String DEFAULT_DATABASE;

	private final static Map<String, Redis> poolMap = new HashMap<String, Redis>();

	public static Redis getRedis(String type, String id) {
		if (RedisConstants.isInUse()) {
			Redis redis = poolMap.get(type);
			if (redis == null) {
				log.error("redis（type={}） 没有配置", type);
				return poolMap.get(DEFAULT_DATABASE);
			}
			return redis;
		} else {
			return new Redis(null,null);
		}
	}

	static {
		try {
			Properties is = PropertiesUtil.getProperties("redis");
			String databases = is.getProperty("databases");
			String[] databasesSplit = databases.split(",");
			DEFAULT_DATABASE = is.getProperty("database.default");
			for (int i = 0; i < databasesSplit.length; i++) {
				String database = databasesSplit[i];
				Redis redis = create(is, database);
				poolMap.put(database, redis);
			}
			log.info("redis连接池初始化完毕!!!");
		} catch (Exception e) {
			LogUtil.error("redis连接池初始化失败。" + e.getMessage(), e);
			throw new RuntimeException("redis 启动失败");
		}
	}

	public static void init() {

	}

	/**
	 * 创建redisPool，并加入到poolMap中
	 * 
	 * @param is
	 *            配置
	 * @param type
	 *            配置文件中的配置项前缀
	 * @param key
	 *            创建的redisPool在poolMap中对应的key
	 * @param database
	 *            当前所使用的库
	 */
	private static Redis create(Properties is, String database) {
		if (RedisConstants.isInUse()) {
			String host = is.getProperty(database + ".host");
			// 如果host没有配置,并且redis不是严格模式,就忽略掉该库
			if (StringUtils.isEmpty(host) && !RedisConstants.isStrict()) {
				log.error("redis（pre={}） 没有配置", database);
				return null;
			}
			GenericObjectPoolConfig config = new JedisPoolConfig();
			// 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
			// 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
			config.setMaxTotal(Integer.parseInt(is.getProperty(database + ".maxActive", MAXACTIVE)));
			config.setMaxIdle(Integer.parseInt(is.getProperty(database + ".maxIdle", MAXIDLE)));
			config.setMaxWaitMillis(Long.parseLong(is.getProperty(database + ".maxWait", MAXWAIT)));
			// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
			int timeout = Integer.parseInt(is.getProperty(database + ".timeout", TIMEOUT));
			String password = is.getProperty(database + ".password", null);
			// config.setTestOnBorrow(configurationManager.getRedisTestOnBorrow());
			String type = is.getProperty(database + ".type", RedisConnectType.Sentinel.toString());
			RedisConnectType redisConnectType = RedisConnectType.valueOf(type);
			Pool<Jedis> pool = null;
			switch (redisConnectType) {
			case Sentinel: {
				String masterName = is.getProperty(database + ".masterName", "mymaster");
				pool = new JedisSentinelPoolExt(masterName, host, config, password, timeout, 5, true);
				break;
			}
			case Simple: {
				int port = Integer.parseInt(is.getProperty(database + ".port"));
				int redisDatabase = Integer.parseInt(is.getProperty(database + ".database", "0"));
				pool = new JedisPool(config, host, port, timeout, password, redisDatabase);
				break;
			}
			default:
				break;
			}
			Redis r = new Redis(pool, redisConnectType);
			r.ping();
			if (log.isInfoEnabled()) {
				log.info("redis(" + database + "):" + "{host:" + host + ",maxActive:" + config.getMaxTotal()
						+ ",maxIdle:" + config.getMaxIdle() + ",maxWaitMS:" + config.getMaxWaitMillis() + ",timeout:"
						+ timeout + ",type:" + type + "}");
			}
			return r;
		}
		return null;
	}

}
