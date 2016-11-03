package net.jahhan.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;

public class JsonUtil {

	private static SerializeConfig innerConfig = new SerializeConfig();

	// 默认属性配置
	private static SerializerFeature[] innerFeatures = { SerializerFeature.WriteMapNullValue,
			SerializerFeature.DisableCircularReferenceDetect };

	static {
		innerConfig.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
		innerConfig.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
	}

	/**
	 * 用于显示，不能用于存入redis
	 * 
	 * @param obj
	 * @return
	 */
	public static String getShowJson(Object obj) {
		SerializeWriter out = new SerializeWriter();
		try {
			JSONSerializer serializer = new JSONSerializer(out, innerConfig);
			for (com.alibaba.fastjson.serializer.SerializerFeature feature : innerFeatures) {
				serializer.config(feature, true);
			}
			serializer.write(obj);
			return out.toString();
		} catch (Exception e) {
			return GsonUtil.toJson(obj);
		} finally {
			out.close();
		}
	}

	public static String toJson(Object obj) {
		try {
			return JSON.toJSONString(obj);
		} catch (Exception e) {
			return GsonUtil.toJson(obj);
		}
	}

	public static <T> T parseObject(String json, Class<T> clazz) {
		try {
			return JSON.parseObject(json, clazz);
		} catch (Exception e) {
			return GsonUtil.fromJson(json, clazz);
		}
	}

	public static Map<String, String> parseMap(String json) {
		try {
			return fromJson(json, new StringMapRef());
		} catch (Exception e) {
			return GsonUtil.parseStringMap(json);
		}
	}

	public static Map<String, Object> parseObjectMap(String json) {
		try {
			return JSON.parseObject(json, new ObjectMapRef());
		} catch (Exception e) {
			return GsonUtil.parseMap(json);
		}
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	public static <T> List<T> parseList(String json, Class<T> clazz) {
		try {
			return JSONArray.parseArray(json, clazz);
		} catch (Exception e) {
			List<T> list = new ArrayList<T>();
			List<Object> list2 = GsonUtil.fromJson(json, List.class);
			for (Object obj : list2) {
				list.add(JsonUtil.copyObject(obj, clazz));
			}
			return list;
		}
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		try {
			return JSON.parseObject(json, clazz);
		} catch (Exception e) {
			return GsonUtil.fromJson(json, clazz);
		}
	}

	public static <T> T fromJson(String json, TypeReference<T> type) {
		try {
			return JSON.parseObject(json, type);
		} catch (Exception e) {
			return GsonUtil.fromJson(json, type.getType());
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
	public static String mergeJson(String old, Object updatedObject) {
		Map<String, Object> map = JsonUtil.parseObjectMap(old);
		Map<String, Object> map2 = JsonUtil.parseObjectMap(JsonUtil.toJson(updatedObject));
		map.putAll(map2);
		return JsonUtil.toJson(map);
	}

	public static Map<String, String> parseJsonToMap(String requestJson) {
		try {
			return JSON.parseObject(requestJson, new StringMapRef() {
			}.getType());
		} catch (Exception e) {
			return null;
		}
	}

	public static List<Map<String, String>> parseJsonArrayToMap(String jsonArrayData) {
		try {
			return JSON.parseObject(jsonArrayData, new TypeReference<List<Map<String, String>>>() {
			}.getType());
		} catch (Exception e) {
			return null;
		}
	}

}

class ObjectMapRef extends TypeReference<Map<String, Object>> {

}

class StringMapRef extends TypeReference<Map<String, String>> {

}
