package net.jahhan.jedis;

import lombok.Data;

@Data
public class RedisDataSourceImpl implements RedisDataSource {

    private JedisSentinelPoolExt jedisSentinelPool;

    @Override
    public WriteJedis getRedisWriteClient() {
        return jedisSentinelPool.getWriteResource();
    }

    @Override
    public ReadJedis getRedisReadClient() {
        return jedisSentinelPool.getReadResource();
    }

    @Override
    public void resetReadPool() {
        jedisSentinelPool.resetReadPool();
    }
}
