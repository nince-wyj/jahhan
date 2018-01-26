package net.jahhan.common.extension.constant;

import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.utils.PropertiesUtil;

/**
 * @author nince
 */
@Slf4j
public class BaseConfiguration {
	public static String FRAMEWORK_PATH;
	public static String SERVICE_PATH;
	public static String SYSTEM_ERROR_CODE_LEVEL = "JAHHAN";
	public static String SERVICE = "";
	public static boolean IS_DEBUG = false;
	public static Integer GLOBAL_EXPIRE_SECOND;
	public static String INTERFACE_SUFFIX = "Intf";
	public static InjectType INJECT_TYPE = InjectType.guice;

	static {
		Properties property = PropertiesUtil.getProperties("base");
		try {
			FRAMEWORK_PATH = property.getProperty("path.framework");
			SERVICE_PATH = property.getProperty("path.service");
			IS_DEBUG = Boolean.parseBoolean(property.getProperty("debug", "false"));
			SERVICE = property.getProperty("serviceCode");
			String globalExpireSecond = property.getProperty("globalExpireSecond", "7200");
			int ttl = NumberUtils.toInt(globalExpireSecond, -1);
			if (ttl > 0) {
				GLOBAL_EXPIRE_SECOND = ttl;
			}
			INTERFACE_SUFFIX = property.getProperty("interface.suffix", "Intf");
			INJECT_TYPE = InjectType.valueOf(property.getProperty("inject.type", InjectType.guice.toString()));
		} catch (Exception ex) {
			log.error("加载系统base.properties配置出错", ex);
			throw new RuntimeException("加载系统base.properties配置出错");
		}
	}

}
