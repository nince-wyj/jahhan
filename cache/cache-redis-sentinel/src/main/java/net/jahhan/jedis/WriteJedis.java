package net.jahhan.jedis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import redis.clients.jedis.Jedis;

@Data
@EqualsAndHashCode(callSuper=false)
public class WriteJedis extends BaseJedis {

    public WriteJedis(Jedis jedis) {
        this.jedis = jedis;
        this.client = jedis.getClient();
    }

    @Override
    public void close() {
        this.jedis.close();
    }

}
