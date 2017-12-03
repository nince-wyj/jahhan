package net.jahhan.cache.repository.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.inject.name.Named;

import net.jahhan.cache.util.SerializerUtil;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.utils.BeanTools;
import net.jahhan.spi.DBCache;

public abstract class AbstractSimpleRepository {

	@Inject
	protected DBCache cache;

	/**
	 * 在redis中存活的时间
	 * 
	 * @return
	 */
	protected int getExistSecond() {
		return BaseConfiguration.GLOBAL_EXPIRE_SECOND;
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
		String key = getKey(id);
		return cache.get(key);
	}

	public byte[] getBytes(String id) {
		String key = getKey(id);
		return cache.getBinary(key.getBytes());
	}

	/**
	 * @param id
	 *            根据id获取json
	 * @param json
	 */
	public void set(String id, Object object) {
		String key = getKey(id);
		cache.setByte(key.getBytes(), SerializerUtil.serializeFrom(object));
	}

	public void del(String id) {
		String key = getKey(id);
		cache.del(key);
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
		cache.del(ids);
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
		return cache.mget(keys);
	}
	
	public List<byte[]> getMultiByteValue(Collection<byte[]> ids) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<byte[]>();
		}
		String[] keys = getKeys(ids.toArray(new String[ids.size()]));
		byte[][] keysB = new byte[ids.size()][];
		for (int i = 0; i < ids.size(); i++) {
			keysB[i] = keys[i].getBytes();
		}
		return cache.mgetByte(keysB);
	}

	/**
	 * 批量获取id对应的值. 注意：这个方法仅适用于不需要分库的表
	 * 
	 * @param <T>
	 * 
	 * @param ids
	 * @return
	 */
	public <T> List<T> getMultiValue(Collection<String> ids, Class<T> clazz) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<T>();
		}
		String[] keys = getKeys(ids.toArray(new String[ids.size()]));
		byte[][] keysB = new byte[ids.size()][];
		for (int i = 0; i < ids.size(); i++) {
			keysB[i] = keys[i].getBytes();
		}
		List<byte[]> multi = cache.mgetByte(keysB);
		List<T> result = new ArrayList<>();
		for (byte[] b : multi) {
			T object = SerializerUtil.deserialize(b, clazz);
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
		return cache.mget(keys);
	}

	/**
	 * @param id
	 * @param time
	 *            系统时间，单位是毫秒
	 */
	public void pexpiredAt(String id, long time) {
		String key = getKey(id);
		cache.pexpireAt(key, time);
	}

	public void expired(String id, int seconds) {
		String key = getKey(id);
		cache.expire(key, seconds);
	}

	public boolean exists(String id) {
		String key = getKey(id);
		return cache.exists(key);
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
		byte[] returnBytes = getBytes(id);

		if (returnBytes != null) {
			Object mergeObject = mergeObject(SerializerUtil.deserialize(returnBytes, checkedClass), updatedObj);
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
