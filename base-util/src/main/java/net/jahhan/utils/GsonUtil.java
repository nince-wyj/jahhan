package net.jahhan.utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import net.jahhan.constant.TypeConstants;

public class GsonUtil {

	private static Logger logger = LoggerFactory.getLogger(GsonUtil.class);

	// gson是线程安全，但是某些类为了实现安全，加上了synchronized，比如DateTypeAdapter
	// 所有注册的TypeAdapter都会被封装为TypeAdapterFactory，见ergister方法。
	// 用户自定义的TypeAdapter从第4个开始，见Gson的构造方法。
	// registerTypeAdapter(Date.class, new
	// DateTimeTypeAdapter())注册的Adapter只对本类型有效。 不能作用于子类。
	// Timestamp之所以也能用,参加TypeAdapters.TIMESTAMP_FACTORY，该adapter使用Data的adapter作为timestamp的序列化和反序列化
	private static Gson createGson() {
		return new GsonBuilder().disableHtmlEscaping().registerTypeAdapter(Date.class, new DateTimeTypeAdapter())
				.create();
	}

	private static Gson[] gsons;

	private static volatile int index = 0;

	static {
		gsons = new Gson[10];
		for (int i = 0; i < gsons.length; i++) {
			gsons[i] = createGson();
		}
	}

	private static Gson getGson() {
		int i = ++index;
		if (i < 0) {
			index = Math.max(0, -index);
			i = 0;
			logger.info("gson 下标重新生成 i:{},index:{}", i, index);
		}
		return gsons[i % gsons.length];

	}

	public static String toJson(Object obj) {
		return getGson().toJson(obj);
	}

	public static <T> T parseObject(String json, Class<T> clz) {
		return getGson().fromJson(json, clz);
	}

	public static <T> T fromJson(String json, Class<T> clz) {
		return getGson().fromJson(json, clz);
	}

	public static <T> T fromJson(String json, Type type) {
		return getGson().fromJson(json, type);
	}

	public static Map<String, Object> parseMap(String json) {
		return getGson().fromJson(json, TypeConstants.objectMap);
	}

	public static Map<String, String> parseStringMap(String json) {
		return getGson().fromJson(json, TypeConstants.stringMap);
	}
}

/*
 * 
 */
class DateTimeTypeAdapter extends TypeAdapter<Date> {
	private Logger logger = LoggerFactory.getLogger(DateTimeTypeAdapter.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public Date read(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}
		return deserializeToDate(in.nextString());
	}

	private synchronized Date deserializeToDate(String json) {
		if (json == null) {
			return null;
		}
		try {
			try {
				return sdf2.parse(json);
			} catch (Exception e) {
			}
			try {
				return sdf.parse(json);
			} catch (Exception e) {
			}
			// mongoDB会将timestamp序列化为double格式，这是为了兼容mongoDB
			try {
				return new Date(Long.parseLong(json));
			} catch (Exception e) {
			}
			double d = Double.parseDouble(json);
			return new Date((long) d);
		} catch (RuntimeException e) {
			logger.error("{}无法解析成日期类型", json);
			throw e;
		}
	}

	@Override
	public synchronized void write(JsonWriter out, Date value) throws IOException {
		if (value == null) {
			out.nullValue();
			return;
		}
		out.value(value.getTime());
	}

}
