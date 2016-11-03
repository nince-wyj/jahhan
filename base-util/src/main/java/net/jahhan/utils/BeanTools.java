package net.jahhan.utils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;

import net.jahhan.utils.apache.convert.DateConverter;

public class BeanTools {
	private static ConvertUtilsBean convertUtils = BeanUtilsBean.getInstance().getConvertUtils();

	static {
		convertUtils.register(new DateConverter(), Date.class);
	}

	public static <S, D> D convertType(Object obj, Class<S> srcClass, Class<D> destClass) {
		Converter converter = convertUtils.lookup(srcClass, destClass);
		return converter.convert(destClass, obj);
	}

	public static Map<String,Object> toMap(Object model) {
		Map<String,Object> m = new LinkedHashMap<>();
		Field[] fields = model.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				m.put(field.getName(), field.get(model));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return m;
	}

	@SuppressWarnings("rawtypes")
	public static void copyFromMap(Object dest, Map src) {
		Field[] fields = dest.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				String name = field.getName();
				Object value = src.get(name);
				if (value != null) {
					field.set(dest, convertType(value, value.getClass(), field.getType()));
				}
			} catch (Exception e) {
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static void copyBean(Object dest, Object src) {
		Map srcMap = null;
		if (src instanceof Map) {
			srcMap = (Map) src;
		} else {
			srcMap = toMap(src);
		}
		copyFromMap(dest, srcMap);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] values(Class<T> c) {
		try {
			return (T[]) c.getMethod("values").invoke(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] objectToData(Object obj) {
		try {
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bOut);
			out.writeObject(obj);
			out.close();
			bOut.close();
			return bOut.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getUuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}