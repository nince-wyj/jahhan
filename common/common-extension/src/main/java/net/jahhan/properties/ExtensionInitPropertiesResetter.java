package net.jahhan.properties;

import java.util.Properties;

import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.annotation.PropertiesFile;
import net.jahhan.common.extension.api.PropertiesResetter;

@PropertiesFile("extensionInit")
@Order(1)
public class ExtensionInitPropertiesResetter extends PropertiesResetter {

	@Override
	public void reset(Properties properties) {
		// properties.setProperty(SerializerHandler.class.getName(), "java");
	}

}
