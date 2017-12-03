package net.jahhan.common.extension.utils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.utils.properties.PropertiesInit;

@Slf4j
public class PropertiesUtil {

	static {
		ClassLoader classLoader = PropertiesUtil.class.getClassLoader();
		URL url = classLoader.getResource("/");
		String resourcePath = null;
		String osName = System.getProperty("os.name");
		log.debug("file url：" + url);
		if (null != url) {
			resourcePath = url.getPath();
			if (null != osName && osName.contains("Windows")) {

			} else {
				String[] split = resourcePath.split(":");
				resourcePath = split[0];
			}

		} else {
			ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
			url = threadClassLoader.getResource("/");
			log.debug("file url2：" + url);
			if (null != url) {
				resourcePath = url.getPath();
				if (null != osName && osName.contains("Windows")) {

				} else {
					String[] split = resourcePath.split(":");
					resourcePath = split[0];
				}
			} else {
				String envClassPath = System.getProperty("java.class.path");
				String[] classPath = envClassPath.split(";");
				if (classPath.length <= 1) {
					classPath = envClassPath.split(":");
				}
				if (classPath.length > 1) {
					for (String path : classPath) {
						if (path.endsWith("classes")) {
							resourcePath = path;
							break;
						}
						if (path.contains("apache-jmeter") && path.contains("ext")) {
							File file = new File(path);
							resourcePath = file.getParent();
						}
					}
				}
				if (null == resourcePath) {
					resourcePath = classPath[0];
				}
			}
		}

		File file = new File(resourcePath);
		File[] tempList = file.listFiles();
		for (File f : tempList) {
			String fileName = f.getName();
			if (f.isFile() && fileName.endsWith(".properties")) {
				String fileSortName = fileName.replace(".properties", "");
				Properties properties = load(f.getName());
				if (fileSortName.equals("system")) {
					Properties sysProperties = properties;
					Iterator<Object> iterator = sysProperties.keySet().iterator();
					while (iterator.hasNext()) {
						String key = (String) iterator.next();
						System.setProperty(key, sysProperties.getProperty(key));
					}
					PropertiesInit.reset("system", System.getProperties());
				} else {
					PropertiesInit.reset(fileSortName, properties);
				}
				log.debug("加载文件：" + fileName);
			}
		}
	}

	public static Properties getProperties(String fileSortName) {
		return PropertiesInit.get(fileSortName, new Properties());
	}

	public static String get(String fileSortName, String key) {
		Properties properties = getProperties(fileSortName);
		if (null != properties) {
			return properties.getProperty(key);
		}
		return null;
	}

	private static Properties load(String fileName) {
		Properties props = new Properties();

		try (InputStream is = PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName)) {
			if (is != null) {
				try (InputStreamReader reader = new InputStreamReader(is, "UTF-8")) {
					props.load(reader);
				} catch (Exception e) {
					LogUtil.error("error when load props " + fileName, e);
				}
			}
		} catch (Exception e) {
			LogUtil.error("error when load props " + fileName, e);
		}
		return props;
	}
}
