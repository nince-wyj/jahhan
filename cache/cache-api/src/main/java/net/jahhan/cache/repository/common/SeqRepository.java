package net.jahhan.cache.repository.common;

import net.jahhan.context.BaseContext;
import net.jahhan.spi.DBSeqCache;

/**
 * 用于生成自增长的数字
 */
public class SeqRepository {
	
	public static long inc(String key) {
		DBSeqCache cache = BaseContext.CTX.getInjector().getInstance(DBSeqCache.class);
		return cache.incr(key);
	}

	public static long incrBy(String key, long addValue) {
		DBSeqCache cache = BaseContext.CTX.getInjector().getInstance(DBSeqCache.class);
		return cache.incrBy(key,addValue);
	}

	public static void set(String key, long value) {
		DBSeqCache cache = BaseContext.CTX.getInjector().getInstance(DBSeqCache.class);
		cache.set(key, value);
	}
}
