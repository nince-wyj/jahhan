package net.jahhan.cache.repository.common;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.constant.BaseConfiguration;

/**
 * 用于生成自增长的数字
 */
@Slf4j
public class SeqRepository {

	private static Redis getRedis() {
		return RedisFactory.getRedis(RedisConstants.SEQ, null);
	}

	/**
	 * 要统一使用小写
	 * 
	 * @param key
	 * @return
	 */
	public static long inc(String key) {
		long ret = getRedis().incr(BaseConfiguration.SERVICE + "_" + key);
		if (ret < 0) {
			log.warn("序列号生成失败！redis未开启或者连接失败！");
			return 0;
		}
		return ret;
	}

	/**
	 * 增加addValue
	 * 
	 * @param key
	 * @param addValue
	 * @return
	 */
	public static long incrBy(String key, long addValue) {
		return getRedis().incrBy(key, addValue);
	}

	public static void set(String key, long value) {
		Redis redis = getRedis();
		// 只能设置为更大的值，不能设置为更小的
		if (redis.incr(key) >= value) {
			return;
		}
		redis.set(key, String.valueOf(value));
	}
}
