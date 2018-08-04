package net.jahhan.common.extension.api;

import java.util.Properties;
import java.util.Set;

public abstract class PropertiesResetter {
	private Properties fileProperties;

	public Properties init(Properties properties) {
		this.fileProperties = properties;
		Properties tempProperties = new Properties();
		reset(tempProperties);
		Set<Object> keySet = fileProperties.keySet();
		for (Object keyO : keySet) {
			String key = (String) keyO;
			String property = fileProperties.getProperty(key);
			if (null != property) {
				tempProperties.setProperty(key, property);
			}
		}
		return (Properties) tempProperties.clone();
	}

	public abstract void reset(Properties properties);
}
