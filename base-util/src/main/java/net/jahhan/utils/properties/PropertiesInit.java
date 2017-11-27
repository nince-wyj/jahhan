package net.jahhan.utils.properties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.annotation.PropertiesFile;
import net.jahhan.api.PropertiesResetter;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.exception.FrameworkException;
import net.jahhan.utils.Assert;
import net.jahhan.utils.ClassScaner;

public class PropertiesInit {
	private final static Logger logger = LoggerFactory.getLogger("PropertiesInit");
	private static Map<String, PropertiesResetter> propertiesResetterMap = new HashMap<>();

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
					PropertiesResetter put = propertiesResetterMap.put(propertiesFile.value(), propertiesResetter);
					Assert.isTrue(null == put, "配置文件重复：" + propertiesFile.value(), SystemErrorCode.INIT_ERROR);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			FrameworkException.throwException(SystemErrorCode.INIT_ERROR, "配置文件错误");
		}
	}

	public static void reset(String fileName, Properties properties) {
		PropertiesResetter propertiesResetter = propertiesResetterMap.get(fileName);
		if (null != propertiesResetter) {
			propertiesResetter.init(properties);
		}
	}

	public static Properties get(String fileName, Properties properties) {
		PropertiesResetter propertiesResetter = propertiesResetterMap.get(fileName);
		if (null != propertiesResetter) {
			return propertiesResetter.getProperties();
		}
		return properties;
	}
}
