package net.jahhan.properties;

import java.util.Properties;

import net.jahhan.annotation.PropertiesFile;
import net.jahhan.api.PropertiesResetter;

@PropertiesFile("web")
public class WebPropertiesResetter extends PropertiesResetter {

	@Override
	public Properties reset(Properties properties) {
		properties.setProperty("act.store", "false");
		properties.setProperty("act.recordTimeConsume", "false");
		properties.setProperty("html.intercept", "false");
		properties.setProperty("html.allowAllOrigin", "false");
		properties.setProperty("act.async.timeOut", "90000");
		properties.setProperty("session.strategy", "MULTI");
		return properties;
	}

}
