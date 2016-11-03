package net.jahhan.cache;

import redis.clients.jedis.Jedis;

public interface JedisCallBackHandler<T> {
    public T invoke(Jedis jedis);
}
