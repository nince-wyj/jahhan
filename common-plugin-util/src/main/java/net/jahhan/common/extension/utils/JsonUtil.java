package net.jahhan.common.extension.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;

import net.jahhan.spi.common.Serialization;

public class JsonUtil {

	private static Serialization serializer = ExtensionUtil.getExtension(Serialization.class, "fastjson");

	public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
		T readObject = null;
		try {
			ObjectInput deserialize = serializer.deserialize(new ByteArrayInputStream(bytes));
			readObject = deserialize.readObject(clazz);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return readObject;
	}

	public static byte[] serializeFrom(Object object) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutput objectOutput;
		try {
			objectOutput = serializer.serialize(byteArrayOutputStream);
			objectOutput.writeObject(object);
			objectOutput.flushBuffer();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			LogUtil.error(e.getMessage(), e);
		}
		return null;
	}

	public static String toJson(Object obj) {
		byte[] serializeFrom = serializeFrom(obj);
		return new String(serializeFrom);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> parseMap(String json) {
		try {
			return fromJson(json, Map.class);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			return null;
		}
	}

	public static <T> T parseObject(String json, Class<T> clazz) {
		return deserialize(json.getBytes(), clazz);
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		try {
			return parseObject(json, clazz);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
			return null;
		}
	}

	public static Object copyObject(Object source) {
		if (source == null) {
			return null;
		}
		String json = JsonUtil.toJson(source);
		return JsonUtil.fromJson(json, source.getClass());
	}

	public static <T> T copyObject(Object source, Class<T> clz) {
		if (source == null) {
			return null;
		}
		String json = JsonUtil.toJson(source);
		return JsonUtil.fromJson(json, clz);
	}

	/**
	 * 将partUpdate的内容应用到json中去。不支持内嵌对象
	 * 
	 * @param old
	 *            json格式的旧对象
	 * @param partUpdate
	 *            用于更新的对象
	 * @return
	 * @author nince
	 */
	@SuppressWarnings("unchecked")
	public static String mergeJson(String old, Object updatedObject) {
		Map<String, String> map = parseMap(old);
		Map<String, String> map2 = parseObject(toJson(updatedObject), Map.class);
		map.putAll(map2);
		return toJson(map);
	}

}
