package net.jahhan.jedis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import redis.clients.jedis.Jedis;


@Data
@EqualsAndHashCode(callSuper = false)
public class BaseJedis extends Jedis {
	protected Jedis jedis;
}
