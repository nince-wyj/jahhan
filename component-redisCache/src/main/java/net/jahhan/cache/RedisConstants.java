package net.jahhan.cache;

/**
 * table开头的表示是集合
 */
public interface RedisConstants {

	/**
	 * 一天这算为秒数
	 */
	int DAYINSECONDS = 24 * 60 * 60;

	/**
	 * 用于redis相关的log
	 */
	String LOGGER_REDIS = "net.jahhan.redis";

	/**
	 * 公用数据表
	 */
	String TABLE_COMMON = "common";
	
	/**
	 * 数据库缓存
	 */
	String TABLE_SYSTEM = "cache";
	
	/**
	 * session库
	 */
	String TABLE_SESSION = "session";
	/**
	 * seq库
	 */
	String TABLE_SEQ = "seq";
	/**
	 * 消息队列库
	 */
	String TABLE_MQ = "mq";
}
