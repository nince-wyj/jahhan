package net.jahhan.common.extension.utils.properties;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.annotation.PropertiesFile;
import net.jahhan.common.extension.api.PropertiesResetter;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.ClassScaner;

public class PropertiesInit {
	private static Map<String, Properties> propertiesMap = new HashMap<>();
	private static Map<String, Map<Integer, PropertiesResetter>> propertiesResetterMap = new HashMap<>();

	static {
		try {
			String[] packages = new String[] { "net.jahhan.properties" };
			List<String> classNameList = new ClassScaner().parse(packages);
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

			for (String className : classNameList) {
				Class<?> clazz = classLoader.loadClass(className);
				if (clazz.isAnnotationPresent(PropertiesFile.class)
						&& PropertiesResetter.class.isAssignableFrom(clazz)) {
					PropertiesFile propertiesFile = clazz.getAnnotation(PropertiesFile.class);
					PropertiesResetter propertiesResetter = (PropertiesResetter) clazz.newInstance();
					propertiesResetter.init(new Properties());
					Order order = clazz.getAnnotation(Order.class);
					Map<Integer, PropertiesResetter> map = propertiesResetterMap.get(propertiesFile.value());
					if (null == map) {
						map = new TreeMap<>(new Comparator<Integer>() {
							public int compare(Integer a, Integer b) {
								return b - a;
							}
						});
						propertiesResetterMap.put(propertiesFile.value(), map);
					}
					if (null != order) {
						map.put(order.value(), propertiesResetter);
					} else {
						map.put(0, propertiesResetter);
					}
				}
			}
			Set<Entry<String, Map<Integer, PropertiesResetter>>> propertiesEntrySet = propertiesResetterMap.entrySet();
			for (Entry<String, Map<Integer, PropertiesResetter>> propertiesEntryMap : propertiesEntrySet) {
				Map<Integer, PropertiesResetter> value = propertiesEntryMap.getValue();
				Set<Entry<Integer, PropertiesResetter>> propertiesTreeEntrySet = value.entrySet();
				Properties properties = new Properties();
				for (Entry<Integer, PropertiesResetter> entry : propertiesTreeEntrySet) {
					PropertiesResetter propertiesResetter = entry.getValue();
					properties = propertiesResetter.init(properties);
				}
				propertiesMap.put(propertiesEntryMap.getKey(), properties);
			}
		} catch (Exception e) {
			JahhanException.throwException(JahhanErrorCode.INIT_ERROR, "配置文件错误", e);
		}
	}

	public static void reset(String fileName, Properties properties) {
		Map<Integer, PropertiesResetter> propertiesResetterTreeMap = propertiesResetterMap.get(fileName);
		if (null != propertiesResetterTreeMap) {
			Set<Entry<Integer, PropertiesResetter>> propertiesTreeEntrySet = propertiesResetterTreeMap.entrySet();
			for (Entry<Integer, PropertiesResetter> entry : propertiesTreeEntrySet) {
				PropertiesResetter propertiesResetter = entry.getValue();
				properties = propertiesResetter.init(properties);
			}
		}
		propertiesMap.put(fileName, properties);
	}

	public static Properties get(String fileName, Properties properties) {
		Properties propertiesInMap = propertiesMap.get(fileName);
		if (null != propertiesInMap) {
			return propertiesInMap;
		}
		reset(fileName, properties);
		return properties;
	}
}
