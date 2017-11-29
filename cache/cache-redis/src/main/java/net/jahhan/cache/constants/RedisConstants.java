package net.jahhan.cache.constants;

import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;

import net.jahhan.common.extension.utils.PropertiesUtil;

public class RedisConstants {
	/**
	 * true为严格模式,redis的库不存在,就抛异常.
	 */
	private static Boolean strict;

	/**
	 * redis是否启用
	 */
	private static Boolean isInUse;

	public final static String SEQ = "seq";
	public final static String GLOBAL_LOCK = "globalLock";
	public final static String SESSION = "session";
	public final static String TABLE_CACHE = "cache";

	static {
		Properties property = PropertiesUtil.getProperties("base");
		try {
			RedisConstants.setInUse(BooleanUtils.toBoolean(property.getProperty("redis.inuse", "true")));
			RedisConstants.setStrict(BooleanUtils.toBoolean(property.getProperty("redis.isstrict", "true")));

		} catch (Exception ex) {
			throw new RuntimeException("加载系统配置出错");
		}
	}

	public static Boolean isStrict() {
		return strict;
	}

	public static void setStrict(Boolean strict) {
		RedisConstants.strict = strict;
	}

	public static Boolean isInUse() {
		return isInUse;
	}

	public static void setInUse(Boolean isInUse) {
		RedisConstants.isInUse = isInUse;
	}
}