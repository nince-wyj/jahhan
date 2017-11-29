package net.jahhan.cache;

import net.jahhan.common.extension.utils.LogUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class JedisTemplate {

	private JedisPool pool;

	public JedisTemplate(JedisPool pool) {
		this.pool = pool;
	}

	public <T> T execute(JedisCallBackHandler<T> callback) {
		Jedis jedis = null;
		for (int i = 0; i < 3; i++) {
			try {
				jedis = pool.getResource();
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
					pool.returnResource(jedis);
				}
			}
		}
		return null;
	}
}
