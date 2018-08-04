package net.jahhan.properties;

import java.util.Properties;

import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.annotation.PropertiesFile;
import net.jahhan.common.extension.api.PropertiesResetter;

@PropertiesFile("base")
@Order(2)
public class EhcacheBasePropertiesResetter extends PropertiesResetter {

	@Override
	public void reset(Properties properties) {
		properties.setProperty("cachePath", "z:\\");
	}
}
