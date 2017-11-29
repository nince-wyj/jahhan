package net.jahhan.jedis;

public interface RedisDataSource {

    WriteJedis getRedisWriteClient();

    ReadJedis getRedisReadClient();

    void resetReadPool();
}
