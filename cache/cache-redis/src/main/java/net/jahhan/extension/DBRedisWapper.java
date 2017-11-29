package net.jahhan.extension;

import java.util.List;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.DBCache;

@Extension("redis")
public class DBRedisWapper implements DBCache {

	@Override
	public void setEx(byte[] key, int seconds, byte[] value) {
		Redis redis = RedisFactory.getMainRedis(RedisConstants.TABLE_CACHE, null);
		redis.setEx(key, seconds, value);
	}

	@Override
	public void expire(String cachedKey, int seconds) {
		Redis redis = RedisFactory.getMainRedis(RedisConstants.TABLE_CACHE, null);
		redis.expire(cachedKey, seconds);
	}

	@Override
	public String get(String key) {
		Redis redis = RedisFactory.getReadRedis(RedisConstants.TABLE_CACHE, null);
		return redis.get(key);
	}

	@Override
	public byte[] getBinary(byte[] key) {
		Redis redis = RedisFactory.getReadRedis(RedisConstants.TABLE_CACHE, null);
		return redis.getBinary(key);
	}

	@Override
	public String setByte(byte[] key, byte[] value) {
		Redis redis = RedisFactory.getMainRedis(RedisConstants.TABLE_CACHE, null);
		return redis.setByte(key, value);
	}

	@Override
	public Long del(String... keys) {
		Redis redis = RedisFactory.getMainRedis(RedisConstants.TABLE_CACHE, null);
		return redis.del(keys);
	}

	@Override
	public List<String> mget(String[] keys) {
		Redis redis = RedisFactory.getReadRedis(RedisConstants.TABLE_CACHE, null);
		return redis.mget(keys);
	}

	@Override
	public List<byte[]> mgetByte(byte[][] keys) {
		Redis redis = RedisFactory.getReadRedis(RedisConstants.TABLE_CACHE, null);
		return redis.mgetByte(keys);
	}

	@Override
	public Long pexpireAt(String cachedKey, long time) {
		Redis redis = RedisFactory.getMainRedis(RedisConstants.TABLE_CACHE, null);
		return redis.pexpireAt(cachedKey, time);
	}

	@Override
	public boolean exists(String key) {
		Redis redis = RedisFactory.getReadRedis(RedisConstants.TABLE_CACHE, null);
		return redis.exists(key);
	}

	@Override
	public void setEx(String key, int seconds, String value) {
		Redis redis = RedisFactory.getMainRedis(RedisConstants.TABLE_CACHE, null);
		redis.setEx(key, seconds, value);
	}

}
