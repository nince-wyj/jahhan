package net.jahhan.properties;

import java.util.Properties;

import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.annotation.PropertiesFile;
import net.jahhan.common.extension.api.PropertiesResetter;
import net.jahhan.common.extension.constant.InjectType;
import net.jahhan.spring.Application;

@PropertiesFile("base")
@Order(10)
public class BasePropertiesResetter extends PropertiesResetter {

	@Override
	public void reset(Properties properties) {
		properties.setProperty("applicationClasses", Application.class.getName());
		properties.setProperty("inject.type", InjectType.springboot.toString());
	}

}
