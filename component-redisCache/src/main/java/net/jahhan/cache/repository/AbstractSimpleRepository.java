package net.jahhan.cache.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.jahhan.cache.JedisCallBackHandler;
import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisConfigurationManger;
import net.jahhan.cache.RedisFactory;
import net.jahhan.context.ApplicationContext;
import net.jahhan.handler.SerializerHandler;
import net.jahhan.utils.BeanTools;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public abstract class AbstractSimpleRepository {
	/**
	 * 在redis中存活的时间
	 * 
	 * @return
	 */
	protected int getExistSecond() {
		return RedisConfigurationManger.getGlobalExpireSecond();
	}

	/**
	 * 获取redis对应的key，一般是在id前面加个前缀
	 * 
	 * @param k
	 *            记录的id
	 * @return
	 */
	protected abstract String getKey(String id);

	/**
	 * 获取本Repository要操作的表名称
	 * 
	 * @return
	 */
	protected abstract String getType();

	/**
	 * 根据id获取值
	 * 
	 * @param id
	 * @return
	 */
	public String get(String id) {
		Redis redis = RedisFactory.getReadRedis(getType(), id);
		String key = getKey(id);
		return redis.get(key);
	}
	
	public byte[] getBytes(String id) {
		Redis redis = RedisFactory.getReadRedis(getType(), id);
		String key = getKey(id);
		return redis.getBinary(key.getBytes());
	}

	/**
	 * @param id
	 *            根据id获取json
	 * @param json
	 */
	public void set(String id, Object object) {
		Redis redis = RedisFactory.getMainRedis(getType(), id);
		String key = getKey(id);
		SerializerHandler serializer = ApplicationContext.CTX.getSerializer();
		redis.setByte(key.getBytes(), serializer.serializeFrom(object));
	}

	public void del(String id) {
		Redis redis = RedisFactory.getMainRedis(getType(), id);
		String key = getKey(id);
		redis.del(key);
	}

	protected String[] getKeys(String[] ids) {
		String[] keys = new String[ids.length];
		for (int i = 0; i < ids.length; i++) {
			keys[i] = getKey(ids[i]);
		}
		return keys;
	}

	/**
	 * 只能在不分库的表中使用
	 * 
	 * @param ids
	 */
	public void delMulti(String[] ids) {
		if (ids == null || ids.length == 0) {
			return;
		}
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		redis.del(ids);
	}

	/**
	 * 批量获取id对应的值. 注意：这个方法仅适用于不需要分库的表
	 * 
	 * @param ids
	 * @return
	 */
	public List<String> getMultiValue(Collection<String> ids) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<String>();
		}
		String[] keys = getKeys(ids.toArray(new String[ids.size()]));
		Redis redis = RedisFactory.getReadRedis(getType(), "1");
		return redis.mget(keys);
	}

	/**
	 * 批量获取id对应的值. 注意：这个方法仅适用于不需要分库的表
	 * @param <T>
	 * 
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getMultiValue(Collection<String> ids,Class<T> clazz) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<T>();
		}
		String[] keys = getKeys(ids.toArray(new String[ids.size()]));
		byte[][] keysB = new byte[ids.size()][];
		for(int i=0;i<ids.size();i++){
			keysB[i]=keys[i].getBytes();
		}
		Redis redis = RedisFactory.getReadRedis(getType(), "1");
		List<byte[]> multi = redis.mgetByte(keysB);
		SerializerHandler serializer = ApplicationContext.CTX.getSerializer();
		List<T> result = new ArrayList<>();
		for(byte[] b:multi){
			T object = (T) serializer.deserializeInto(b);
			result.add(object);
		}
		return result;
	}
	
	/**
	 * 批量获取id对应的值.值的顺序和ids的顺序一致 注意：这个方法仅适用于不需要分库的表
	 * 
	 * @param ids
	 * @return
	 */
	public List<String> getMultiValue(String[] ids) {
		if (ids == null || ids.length == 0) {
			return new ArrayList<String>();
		}
		String[] keys = getKeys(ids);
		Redis redis = RedisFactory.getReadRedis(getType(), "1");
		return redis.mget(keys);
	}

	/**
	 * 批量设置id的值。注意只在部分库的情况下才能使用
	 * 
	 * @param ids
	 * @param jsons
	 * @return
	 */
	public void setMultiValue(String[] ids, final String[] jsons) {
		if (ids == null || ids.length == 0) {
			return;
		}
		final String[] keys = getKeys(ids);
		Redis redis = RedisFactory.getMainRedis(getType(), "1");
		redis.getTemplate().execute(new JedisCallBackHandler<Void>() {
			public Void invoke(Jedis jedis) {
				Transaction trans = jedis.multi();
				for (int i = 0; i < keys.length; i++) {
					trans.set(keys[i], jsons[i]);
				}
				trans.exec();
				return null;
			}
		});
	}

	/**
	 * @param id
	 * @param time
	 *            系统时间，单位是毫秒
	 */
	public void pexpiredAt(String id, long time) {
		Redis redis = RedisFactory.getMainRedis(getType(), id);
		String key = getKey(id);
		redis.pexpireAt(key, time);
	}

	public void expired(String id, int seconds) {
		Redis redis = RedisFactory.getMainRedis(getType(), id);
		String key = getKey(id);
		redis.expire(key, seconds);
	}

	public boolean exists(String id) {
		Redis redis = RedisFactory.getReadRedis(getType(), id);
		String key = getKey(id);
		return redis.exists(key);
	}

	/**
	 * 部分更新对象的时候，如果redis中有当前对象，将当前对象取出，更新对象，然后刷新到缓存
	 * 
	 * @param id
	 *            redis中的id，注意：不是key
	 * @param updatedObj
	 *            部分更新的对象
	 * @param checkedClass
	 *            用于校验updatedObj，如果updatedObj不能转义为该类型，就不做操作
	 * @author nince
	 */
	protected void merge(String id, Object updatedObj, Class<?> checkedClass) {
		if (!checkedClass.isInstance(updatedObj)) {
			return;
		}
		SerializerHandler serializer = ApplicationContext.CTX.getSerializer();
		byte[] returnBytes = getBytes(id);
		if (returnBytes != null) {
			Object mergeObject = mergeObject(serializer.deserializeInto(returnBytes), updatedObj);
			set(id, mergeObject);
			expired(id, getExistSecond());
		}
	}

	private static Object mergeObject(Object old, Object updatedObject) {
		Map<String, Object> map = BeanTools.toMap(old);
		Map<String, Object> map2 = BeanTools.toMap(updatedObject);
		map.putAll(map2);
		BeanTools.copyFromMap(updatedObject, map);
		return old;
	}
}
