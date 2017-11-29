package net.jahhan.properties;

import java.util.Properties;

import net.jahhan.api.PropertiesResetter;
import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.annotation.PropertiesFile;

@PropertiesFile("extensionInit")
@Order(1)
public class ExtensionInitPropertiesResetter extends PropertiesResetter {

	@Override
	public void reset(Properties properties) {
		// properties.setProperty(SerializerHandler.class.getName(), "java");
	}

}
