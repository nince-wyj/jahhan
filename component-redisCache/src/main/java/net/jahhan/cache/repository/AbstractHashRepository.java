package net.jahhan.cache.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;

/**
 * hashset类型的redis
 * 
 * @author nince
 */
public abstract class AbstractHashRepository {

	/**
	 * 整个集合在redis中存活的时间.如果返回值小于1，表示永不过期
	 * 
	 * @return
	 */
	protected int getExistSecond() {
		return 3600;
	}

	/**
	 * 获取redis对应的key，一般是在id前面加个前缀
	 * 
	 * @param k
	 *            记录的id
	 * @return
	 */
	protected abstract String getBigKey(String id);

	/**
	 * 获取本Repository要操作的表名称
	 * 
	 * @return
	 */
	protected abstract String getType();

	/**
	 * 获取hash类型的值
	 * 
	 * @param bigId
	 *            大的id
	 * @param smallId
	 *            小的id
	 * @return
	 */
	public String get(String bigId, String smallKey) {
		Redis redis = RedisFactory.getReadRedis(getType(), null);
		return redis.hget(getBigKey(bigId), smallKey);
	}

	/**
	 * 获取hash类型的值
	 * 
	 * @param bigId
	 *            大的id
	 * @param smallId
	 *            小的id
	 * @return
	 */
	public List<String> hmget(String bigId, String[] smallKeys) {
		Redis redis = RedisFactory.getReadRedis(getType(), null);
		String bigKey = getBigKey(bigId);
		return redis.hmget(bigKey, smallKeys);
	}

	public boolean hmset(String bigId, Map<String, String> values) {
		Redis redis = RedisFactory.getReadRedis(getType(), null);
		String bigKey = getBigKey(bigId);
		return redis.hmset(bigKey, values) != null;
	}

	public void set(String bigId, String smallKey, String value) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		String bigKey = getBigKey(bigId);
		redis.hset(bigKey, smallKey, value);
		if (this.getExistSecond() > 0) {
			redis.expire(bigKey, getExistSecond());
		}
	}

	public Long setnx(String bigId, String smallKey, String value) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		String bigKey = getBigKey(bigId);
		Long rt = redis.hsetnx(bigKey, smallKey, value);
		if (this.getExistSecond() > 0) {
			redis.expire(bigKey, getExistSecond());
		}
		return rt;
	}

	public void set(String bigId, Map<String, String> map) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		String bigKey = getBigKey(bigId);
		redis.hmset(bigKey, map);
		if (this.getExistSecond() > 0) {
			redis.expire(bigKey, getExistSecond());
		}
	}

	/**
	 * 更新map类型的数据的值
	 * 
	 * @param bigId
	 * @param smallKey
	 * @param addNum
	 *            可以为负数，表示减少
	 */
	public long incrBy(String bigId, String smallKey, long addNum) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		String bigKey = getBigKey(bigId);
		return redis.hincr(bigKey, smallKey, addNum);
	}

	public void del(String bigId) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		String key = getBigKey(bigId);
		redis.del(key);
	}

	public void del(String bigId, String smallKey) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		String key = getBigKey(bigId);
		redis.hdel(key, smallKey);
	}

	public void del(String bigId, String[] smallKeys) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		String key = getBigKey(bigId);
		redis.hdel(key, smallKeys);
	}

	public boolean exists(String bigId) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		String key = getBigKey(bigId);
		return redis.exists(key);
	}

	public void expired(String bigId, int seconds) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		String key = getBigKey(bigId);
		redis.expire(key, seconds);
	}

	public Map<String, String> getAll(String bigId) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		String key = getBigKey(bigId);
		return redis.hgetAll(key);
	}

	/**
	 * 获取当前库的所有主键
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> keys(String match) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		return redis.keys(match);
	}

	/**
	 * 返回map类型数据的所有二级主键
	 * 
	 * @param key
	 * @return
	 */
	public Set<String> getMapKeys(String key) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		return redis.hkeys(key);
	}

	private static String hsetnx_sha = "ea5e22550bee4f95238953fb9d61c8adf2923424";

	/**
	 * 如果map表为空，则设置一个键值对
	 * 
	 * @param key
	 * @param value
	 * @return 1 if the key was set 0 if the key was not set
	 */
	public Long hsetnx(String bigKey, String smallKey, String value) {
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		return (Long) redis.evalsha(hsetnx_sha, 3, bigKey, smallKey, value);
	}
}
