package net.jahhan.properties;

import java.util.Properties;

import net.jahhan.api.PropertiesResetter;
import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.annotation.PropertiesFile;
import net.jahhan.spi.DataSourcePoolInit;

@PropertiesFile("extensionInit")
@Order(100)
public class C3P0ExtensionInitPropertiesResetter extends PropertiesResetter {

	@Override
	public void reset(Properties properties) {
		properties.setProperty(DataSourcePoolInit.class.getName(), "c3p0");
	}

}
