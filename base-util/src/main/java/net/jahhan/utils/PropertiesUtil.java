package net.jahhan.utils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.utils.properties.PropertiesInit;

public class PropertiesUtil {
	private final static Logger logger = LoggerFactory.getLogger("PropertiesUtil");
	private static Map<String, Properties> propertiesMap = new HashMap<>();

	static {
		URL url = PropertiesUtil.class.getClassLoader().getResource("/logback.xml");
		String resourcePath = null;
		if (null != url) {
			resourcePath = url.getPath();
		} else {
			try {
				resourcePath = Thread.currentThread().getContextClassLoader().getResource("logback.xml").toURI()
						.getPath();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		File file = new File(resourcePath).getParentFile();
		File[] tempList = file.listFiles();
		for (File f : tempList) {
			String fileName = f.getName();
			if (f.isFile() && fileName.endsWith(".properties")) {
				Properties properties = load(f.getName());
				String fileSortName = fileName.replace(".properties", "");
				if (fileSortName.equals("system")) {
					Properties sysProperties = properties;
					Iterator<Object> iterator = sysProperties.keySet().iterator();
					while (iterator.hasNext()) {
						String key = (String) iterator.next();
						System.setProperty(key, sysProperties.getProperty(key));
					}
				} else {
					PropertiesInit.reset(fileSortName, properties);
					propertiesMap.put(fileSortName, PropertiesInit.get(fileSortName, properties));
				}
				logger.debug("加载文件：" + fileName);
			}
		}
		propertiesMap.put("system", System.getProperties());
	}

	public static Properties getProperties(String fileSortName) {
		Properties properties = propertiesMap.get(fileSortName);
		if (null == properties) {
			return PropertiesInit.get(fileSortName, new Properties());
		}
		return properties;
	}

	public static String get(String fileSortName, String key) {
		Properties properties = propertiesMap.get(fileSortName);
		if (null != properties) {
			return properties.getProperty(key);
		}
		return null;
	}

	private static Properties load(String fileName) {
		Properties props = new Properties();

		try (InputStream is = PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName)) {
			logger.debug("debug when load props ");
			logger.debug(String.valueOf(is));
			if (is != null) {
				try (InputStreamReader reader = new InputStreamReader(is, "UTF-8")) {
					props.load(reader);
				} catch (Exception e) {
					logger.error("error when load props " + fileName, e);
				}
			}
		} catch (Exception e) {
			logger.error("error when load props " + fileName, e);
		}
		return props;
	}
}
