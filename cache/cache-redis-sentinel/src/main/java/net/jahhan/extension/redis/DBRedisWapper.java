package net.jahhan.extension.redis;

import java.util.List;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.DBCache;

@Extension("redis")
public class DBRedisWapper implements DBCache {
	Redis redis = RedisFactory.getRedis(RedisConstants.TABLE_CACHE, null);
	
	@Override
	public void setEx(byte[] key, int seconds, byte[] value) {
		redis.setEx(key, seconds, value);
	}

	@Override
	public void expire(String cachedKey, int seconds) {
		redis.expire(cachedKey, seconds);
	}

	@Override
	public String get(String key) {
		return redis.get(key);
	}

	@Override
	public byte[] getBinary(byte[] key) {
		return redis.getBinary(key);
	}

	@Override
	public String setByte(byte[] key, byte[] value) {
		return redis.setByte(key, value);
	}

	@Override
	public Long del(String... keys) {
		return redis.del(keys);
	}

	@Override
	public List<String> mget(String[] keys) {
		return redis.mget(keys);
	}

	@Override
	public List<byte[]> mgetByte(byte[][] keys) {
		return redis.mgetByte(keys);
	}

	@Override
	public Long pexpireAt(String cachedKey, long time) {
		return redis.pexpireAt(cachedKey, time);
	}

	@Override
	public boolean exists(String key) {
		return redis.exists(key);
	}

	@Override
	public void setEx(String key, int seconds, String value) {
		redis.setEx(key, seconds, value);
	}

}
