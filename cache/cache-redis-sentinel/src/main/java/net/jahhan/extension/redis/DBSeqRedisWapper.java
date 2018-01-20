package net.jahhan.extension.redis;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.spi.DBSeqCache;

@Extension("redis")
@Slf4j
public class DBSeqRedisWapper implements DBSeqCache {
	Redis redis = RedisFactory.getRedis(RedisConstants.SEQ, null);

	@Override
	public long incr(String key) {
		long ret = redis.incr(BaseConfiguration.SERVICE + ":" + key);
		if (ret < 0) {
			log.warn("序列号生成失败！redis未开启或者连接失败！");
			return 0;
		}
		return ret;
	}

	@Override
	public long incrBy(String key, long addValue) {
		return redis.incrBy(BaseConfiguration.SERVICE + ":" + key, addValue);
	}

	@Override
	public void set(String key, long value) {
		// 只能设置为更大的值，不能设置为更小的
		if (Long.valueOf(redis.get(key)) >= value) {
			return;
		}
		redis.set(key, String.valueOf(value));
	}

}
