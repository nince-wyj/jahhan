package net.jahhan.cache.repository.common;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;

/**
 * 用于限制做一定事件内访问次数的限制。如果时间已过就返回null。如果次数已满，都返回默认值。
 * 
 * @author nince
 */
public class CountLimitRepository {

	private final static String PRE = "CountLimitRep_";

	private final static String VALUE = "value";

	private final static String COUNT_LEFT = "left";

	protected static String getKey(String id) {
		return PRE + id;
	}

	/**
	 * @param pre
	 *            验证码类型前缀
	 * @param existSecond
	 *            验证码有效期限
	 * @param maxCount
	 *            最大尝试次数
	 */
	public static void set(String id, String value, int seconds, int maxCount) {
		Redis redis = RedisFactory.getRedis(getType(), id);
		String key = getKey(id);
		redis.hset(key, VALUE, value);
		redis.hset(key, COUNT_LEFT, String.valueOf(maxCount));
		redis.expire(key, seconds);
	}

	public static String get(String id, String defaultValue) {
		Redis redis = RedisFactory.getRedis(getType(), id);
		String key = getKey(id);
		String value = redis.hget(key, VALUE);
		if (value == null) {
			return null;
		}
		long left = redis.hincr(key, COUNT_LEFT, -1);
		if (left < 0) {
			return defaultValue;
		}
		return value;
	}

	protected static String getType() {
		return RedisFactory.DEFAULT_DATABASE;
	}

	public static void del(String id) {
		String key = getKey(id);
		Redis redis = RedisFactory.getRedis(getType(), id);
		redis.del(key);

	}

}
