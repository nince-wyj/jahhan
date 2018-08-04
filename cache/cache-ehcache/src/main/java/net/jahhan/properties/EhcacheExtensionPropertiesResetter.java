package net.jahhan.properties;

import java.util.Properties;

import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.annotation.PropertiesFile;
import net.jahhan.common.extension.api.PropertiesResetter;
import net.jahhan.spi.DBCache;
import net.jahhan.spi.DBSeqCache;
import net.jahhan.spi.TokenCache;

@PropertiesFile("extensionInit")
@Order(2)
public class EhcacheExtensionPropertiesResetter extends PropertiesResetter {

	@Override
	public void reset(Properties properties) {
		properties.setProperty(DBCache.class.getName(), "ehcache");
		properties.setProperty(DBSeqCache.class.getName(), "ehcache");
		properties.setProperty(TokenCache.class.getName(), "ehcache");
	}
}
