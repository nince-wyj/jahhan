package net.jahhan.properties;

import java.util.Properties;

import net.jahhan.api.PropertiesResetter;
import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.annotation.PropertiesFile;

@PropertiesFile("base")
@Order(2)
public class EhcacheBasePropertiesResetter extends PropertiesResetter {

	@Override
	public void reset(Properties properties) {
		properties.setProperty("cachePath", "z:\\");
	}
}
