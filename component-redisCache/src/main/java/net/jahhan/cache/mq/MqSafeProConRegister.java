package net.jahhan.cache.mq;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.cache.Redis;
import net.jahhan.mq.MqScaner;
import net.jahhan.utils.PropertiesUtil;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class MqSafeProConRegister extends MqScaner {
	private MqRepository mqRepository = new MqRepository();

	@Override
	protected void register() {
		Redis redis = mqRepository.getRedis();
		Set<String> keySet = proConlisteners.keySet();
		byte[][] keyByteArray = new byte[keySet.size()][];
		String[] keyArray = keySet.toArray(new String[keySet.size()]);
		for (int i = 0, length = keySet.size(); i < length; i++) {
			keyByteArray[i] = keyArray[i].getBytes();
		}
		if (safeProConlisteners.keySet().size() > 0) {
			while (true) {
				List<byte[]> pullReturn = redis.bpull(keyByteArray);
				if (null == pullReturn)
					continue;
				Iterator<byte[]> returnIt = pullReturn.iterator();
				String channel = "";
				while (returnIt.hasNext()) {
					byte[] next = returnIt.next();
					String stringNext = new String(next);
					if (proConlisteners.keySet().contains(stringNext)) {
						channel = stringNext;
						continue;
					}
					try {
						safeProConMsgHandle(channel, next);
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			}
		}
	}

	@Override
	public void commit(Long msgId) {
		Redis redis = mqRepository.getRedis();
		redis.del(String.valueOf(msgId));
	}

	public class MqRepository {
		protected Logger logger = LoggerFactory.getLogger(MqRepository.class);
		private String pre = "mq.";
		private final static String MAXACTIVE = "1000"; // 一个pool的最大并发数
		private final static String MAXIDLE = "10"; // pool中最大的空闲数量
		private final static String MAXWAIT = "60000";// 获取一个jedis实例的超时时间，如果超过这个时间，就会抛出异常，单位ms
		private final static String TIMEOUT = "60000";// 用于设置socket的timeout，单位是ms
		private Redis redis;

		public MqRepository() {
			Properties is = PropertiesUtil.getProperties("redis");
			JedisPoolConfig config = new JedisPoolConfig();
			// 控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
			// 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
			config.setMaxTotal(Integer.parseInt(is.getProperty(pre + "maxActive", MAXACTIVE)));
			config.setMaxIdle(Integer.parseInt(is.getProperty(pre + "maxIdle", MAXIDLE)));
			config.setMaxWaitMillis(Long.parseLong(is.getProperty(pre + "maxWait", MAXWAIT)));
			int database = Integer.parseInt(is.getProperty(pre + "database", "0"));
			int port = Integer.parseInt(is.getProperty(pre + "port"));
			String host = is.getProperty(pre + "host");
			int timeout = Integer.parseInt(is.getProperty(pre + "timeout", TIMEOUT));
			String password = is.getProperty(pre + "password", null);
			JedisPool pool = new JedisPool(config, host, port, timeout, password, database);
			redis = new Redis(pool);
		}

		public Redis getRedis() {
			return redis;
		}
	}
}
