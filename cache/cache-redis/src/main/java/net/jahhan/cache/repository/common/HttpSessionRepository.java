package net.jahhan.cache.repository.common;

import net.jahhan.cache.JedisCallBackHandler;
import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.cache.repository.AbstractHashRepository;
import redis.clients.jedis.Jedis;

public class HttpSessionRepository extends AbstractHashRepository {
	@Override
	protected String getBigKey(String id) {
		return id;
	}

	@Override
	protected String getType() {
		return RedisConstants.SESSION;
	}

	@Override
	protected int getExistSecond() {
		return -1;
	}

	public byte[] get(final byte[] sessionId) {
		Redis redis = RedisFactory.getReadRedis(getType(), null);
		return redis.getTemplate().execute(new JedisCallBackHandler<byte[]>() {
			public byte[] invoke(Jedis jedis) {
				return jedis.get(sessionId);
			}
		});
	}

	public String setIfEqual(String sessionId, String oldSession, String newSession) {
		Redis redis = RedisFactory.getReadRedis(getType(), null);
		return redis.setIfEqual(sessionId, oldSession, newSession);
	}
}
