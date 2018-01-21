package net.jahhan.cache;

import net.jahhan.cache.constants.RedisConnectType;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.jedis.JedisSentinelPoolExt;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

public class JedisTemplate {

	private Pool<Jedis> pool;
	private RedisConnectType redisConnectType;

	public JedisTemplate(Pool<Jedis> pool, RedisConnectType redisConnectType) {
		this.pool = pool;
		this.redisConnectType = redisConnectType;
	}

	public Jedis getWriteJedis() {
		switch (redisConnectType) {
		case Sentinel: {
			return ((JedisSentinelPoolExt) pool).getWriteResource();
		}
		case Simple: {
			return pool.getResource();
		}
		default:
			break;
		}
		return null;
	}

	public <T> T executeWrite(JedisCallBackHandler<T> callback) {
		Jedis jedis = null;
		for (int i = 0; i < 3; i++) {
			try {
				switch (redisConnectType) {
				case Sentinel: {
					jedis = ((JedisSentinelPoolExt) pool).getWriteResource();
				}
				case Simple: {
					jedis = pool.getResource();
				}
				default:
					break;
				}
				T t = callback.invoke(jedis);
				return t;
			} catch (JedisConnectionException e) {
				LogUtil.error("redis连接错误！", e);
				if (jedis != null) {
					pool.returnBrokenResource(jedis);
					jedis = null;
				}
			} catch (Exception e) {
				LogUtil.error("redis执行错误！", e);
				return null;
			} catch (Error e) {
				LogUtil.error("redis执行错误！", e);
				return null;
			} finally {
				if (jedis != null) {
					jedis.close();
					// pool.returnResource(jedis);
				}
			}
		}
		return null;
	}

	public <T> T executeRead(JedisCallBackHandler<T> callback) {
		Jedis jedis = null;
		for (int i = 0; i < 3; i++) {
			try {
				switch (redisConnectType) {
				case Sentinel: {
					jedis = ((JedisSentinelPoolExt) pool).getReadResource();
				}
				case Simple: {
					jedis = pool.getResource();
				}
				default:
					break;
				}
				T t = callback.invoke(jedis);
				return t;
			} catch (JedisConnectionException e) {
				LogUtil.error("redis连接错误！", e);
				if (jedis != null) {
					pool.returnBrokenResource(jedis);
					jedis = null;
				}
			} catch (Exception e) {
				LogUtil.error("redis执行错误！", e);
				return null;
			} catch (Error e) {
				LogUtil.error("redis执行错误！", e);
				return null;
			} finally {
				if (jedis != null) {
					jedis.close();
					// pool.returnResource(jedis);
				}
			}
		}
		return null;
	}
}
