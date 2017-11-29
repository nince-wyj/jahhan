package net.jahhan.jedis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Data
@EqualsAndHashCode(callSuper=false)
public class ReadJedis extends BaseJedis {

    private JedisPool jedisPool;

    public ReadJedis(Jedis jedis, JedisPool jedisPool) {
        this.jedis = jedis;
        this.jedisPool = jedisPool;
        this.client = jedis.getClient();
    }

    @Override
    public void close() {
        this.jedis.close();
    }
}
