package net.jahhan.common.extension.utils;

import net.jahhan.common.extension.utils.apache.convert.DateConverter;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.*;

public class BeanTools {
	private static ConvertUtilsBean convertUtils = BeanUtilsBean.getInstance().getConvertUtils();

	static {
		convertUtils.register(new DateConverter(), Date.class);
	}

	public static <S, D> D convertType(Object obj, Class<S> srcClass, Class<D> destClass) {
		if (srcClass.equals(destClass)) {
			return (D) JsonUtil.copyObject(obj);
		}
		Converter converter = convertUtils.lookup(srcClass, destClass);
		return converter.convert(destClass, obj);
	}

	public static Map<String, Object> toMap(Object model) {
		Map<String, Object> m = new LinkedHashMap<>();
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
		List<Field> fields = getSuperClassField(dest.getClass());
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				String name = field.getName();
				Object value = src.get(name);
				if (value != null && !name.equals("serialVersionUID")) {
					field.set(dest, convertType(value, value.getClass(), field.getType()));
				}
			} catch (Exception e) {
			}
		}
	}

	private static List<Field> getSuperClassField(Class<?> clazz) {
		Class<?> superclass = clazz.getSuperclass();
		List<Field> fields = new ArrayList<>();
		if (null != superclass) {
			fields.addAll(getSuperClassField(superclass));
		}
		Field[] declaredFields = clazz.getDeclaredFields();
		fields.addAll(Arrays.asList(declaredFields));
		return fields;
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