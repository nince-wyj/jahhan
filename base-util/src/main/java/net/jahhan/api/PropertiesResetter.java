package net.jahhan.api;

import java.util.Properties;
import java.util.Set;

public abstract class PropertiesResetter {
	private Properties fileProperties;
	private Properties targetProperties;

	public void init(Properties properties) {
		this.fileProperties = properties;
		Properties tempProperties = new Properties();
		tempProperties = reset(tempProperties);
		Set<Object> keySet = fileProperties.keySet();
		for (Object keyO : keySet) {
			String key = (String) keyO;
			String property = fileProperties.getProperty(key);
			if (null != property) {
				tempProperties.setProperty(key, property);
			}
		}
		targetProperties = tempProperties;
	}

	public abstract Properties reset(Properties properties);

	public Properties getProperties() {
		return targetProperties;
	}
}
