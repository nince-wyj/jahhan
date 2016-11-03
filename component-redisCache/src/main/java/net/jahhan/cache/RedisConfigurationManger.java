package net.jahhan.cache;

import java.util.Properties;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.BooleanUtils;

import net.jahhan.utils.PropertiesUtil;

public class RedisConfigurationManger {
	/**
	 * true为严格模式,redis的库不存在,就抛异常.
	 */
	private static Boolean strict;

	/**
	 * 默认的redis全局超时时间
	 */
	private static Integer globalExpireSecond;

	/**
	 * redis是否启用
	 */
	private static Boolean isInUse;

	/**
	 * 接口耗时记录是否持久化
	 */
	private static Boolean consumePersisdence;

	/**
	 * 接口耗时记录保存时间
	 */
	private static Integer consumeCacheDay;

	static {
		Properties property = PropertiesUtil.getProperties("sys_baseconf");
		try {
			RedisConfigurationManger.setInUse(BooleanUtils.toBoolean(property.getProperty("redis.inuse", "true")));
			RedisConfigurationManger.setStrict(BooleanUtils.toBoolean(property.getProperty("redis.isstrict", "true")));
			RedisConfigurationManger.setConsumePersisdence(
					BooleanUtils.toBoolean(property.getProperty("redis.consumePersisdence", "true")));
			RedisConfigurationManger
					.setConsumeCacheDay(NumberUtils.toInt(property.getProperty("redis.consumeCacheDay"), 7));

			String globalExpireSecond = property.getProperty("redis.globalExpireSecond", "7200");
			int ttl = NumberUtils.toInt(globalExpireSecond, -1);
			if (ttl > 0) {
				RedisConfigurationManger.setGlobalExpireSecond(ttl);
			}

		} catch (Exception ex) {
			throw new RuntimeException("加载系统配置出错");
		}
	}

	public static Integer getConsumeCacheDay() {
		return consumeCacheDay;
	}

	public static void setConsumeCacheDay(Integer consumeCacheDay) {
		RedisConfigurationManger.consumeCacheDay = consumeCacheDay;
	}

	public static Boolean isConsumePersisdence() {
		return consumePersisdence;
	}

	public static void setConsumePersisdence(Boolean consumePersisdence) {
		RedisConfigurationManger.consumePersisdence = consumePersisdence;
	}

	public static Boolean isStrict() {
		return strict;
	}

	public static void setStrict(Boolean strict) {
		RedisConfigurationManger.strict = strict;
	}

	public static Integer getGlobalExpireSecond() {
		return globalExpireSecond;
	}

	public static void setGlobalExpireSecond(Integer globalExpireSecond) {
		RedisConfigurationManger.globalExpireSecond = globalExpireSecond;
	}

	public static Boolean isInUse() {
		return isInUse;
	}

	public static void setInUse(Boolean isInUse) {
		RedisConfigurationManger.isInUse = isInUse;
	}
}