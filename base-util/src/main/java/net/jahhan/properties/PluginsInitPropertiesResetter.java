package net.jahhan.properties;

import java.util.Properties;

import net.jahhan.annotation.PropertiesFile;
import net.jahhan.api.PropertiesResetter;
import net.jahhan.spi.SerializerHandler;

@PropertiesFile("pluginsInit")
public class PluginsInitPropertiesResetter extends PropertiesResetter {

	@Override
	public Properties reset(Properties properties) {
		properties.setProperty(SerializerHandler.class.getName(), "java");
		return properties;
	}

}
