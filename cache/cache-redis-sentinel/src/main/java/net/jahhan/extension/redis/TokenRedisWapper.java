package net.jahhan.extension.redis;

import java.util.List;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.TokenCache;

@Extension("redis")
public class TokenRedisWapper implements TokenCache {
	Redis redis = RedisFactory.getRedis(RedisConstants.TOKEN, null);
	
	@Override
	public void setEx(byte[] key, int seconds, byte[] value) {
		redis.setEx(key, seconds, value);
	}

	@Override
	public void expire(String cachedKey, int seconds) {
		redis.expire(cachedKey, seconds);
	}
	
	@Override
	public Long ttl(final String key) {
		return redis.ttl(key);
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
	public void del(String... keys) {
		redis.del(keys);
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

	@Override
	public String set(String key, String value) {
		return redis.set(key, value);
	}

}
