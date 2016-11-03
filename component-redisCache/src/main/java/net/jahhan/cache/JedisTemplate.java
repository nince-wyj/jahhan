package net.jahhan.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class JedisTemplate {
    protected static Logger logger = LoggerFactory.getLogger(RedisConstants.LOGGER_REDIS);

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
                logger.error("redis连接错误！" + e.getMessage(), e);
                if (jedis != null) {
                    pool.returnBrokenResource(jedis);
                    jedis = null;
                }
            } catch (Exception e) {
                logger.error("redis执行错误！" + e.getMessage(), e);
                return null;
            } catch (Error e) {
                logger.error("redis执行错误！" + e.getMessage(), e);
                return null;
            }finally {
                if (jedis != null) {
                    pool.returnResource(jedis);
                }
            }
        }
        return null;
    }
}
