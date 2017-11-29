package net.jahhan.cache;

import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.jedis.JedisSentinelPoolExt;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

public class JedisTemplate {

	private Pool<Jedis> pool;

	public JedisTemplate(Pool<Jedis> pool) {
		this.pool = pool;
	}

	public Jedis getWriteJedis() {
		return ((JedisSentinelPoolExt) pool).getWriteResource();
	}

	public <T> T executeWrite(JedisCallBackHandler<T> callback) {
		Jedis jedis = null;
		for (int i = 0; i < 3; i++) {
			try {
				jedis = ((JedisSentinelPoolExt) pool).getWriteResource();
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
				jedis = ((JedisSentinelPoolExt) pool).getReadResource();
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
