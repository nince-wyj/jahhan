package net.jahhan.cache.util;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisConstants;
import net.jahhan.cache.RedisFactory;

/**
 * 用于生成自增长的数字
 */
public class SeqRepository {
	protected static Logger logger = LoggerFactory
			.getLogger(RedisConstants.LOGGER_REDIS);

	private static Redis getRedis() {
		return RedisFactory.getRedis(RedisConstants.TABLE_SEQ, null);
	}

	/**
	 * 要统一使用小写
	 * 
	 * @param key
	 * @return
	 */
	public static long inc(String key) {
		long ret = getRedis().incr(key);
		if (ret < 0) {
			logger.debug("序列号生成失败！redis未开启或者连接失败！");
			return 0;
		}
		return ret;
	}

	/**
	 * 增加addValue
	 * 
	 * @param key
	 * @param addValue
	 * @return
	 */
	public static long incrBy(String key, long addValue) {
		return getRedis().incrBy(key, addValue);
	}

	public static void set(String key, long value) {
		Redis redis = getRedis();
		// 只能设置为更大的值，不能设置为更小的
		if (redis.incr(key) >= value) {
			return;
		}
		redis.set(key, String.valueOf(value));
	}

	private static final String NS_FILE_IDS = "NS_FILE_IDS";

	/**
	 * 压入文件引用.文件上传到文件服务器时,记录文件服务器返回的fid.可能为废弃文件.文件上传时调用
	 * 
	 * @param fid
	 */
	public static Long pushFid(String fid) {
		return getRedis().zadd(NS_FILE_IDS, fid, System.currentTimeMillis());
	}

	/**
	 * 将文件引用设置为永久文件.文件引用已经存入数据库,成为正式文件.表单提交时调用
	 * 
	 * @param fids
	 */
	public static Long setFidFormal(String... fids) {
		return getRedis().zdel(NS_FILE_IDS, fids);
	}

	/**
	 * 查询过期的fids.用于外部对文件服务器中的文件进行清理.
	 * 
	 * @param ts
	 *            过期止于时间点
	 * @return
	 */
	public static Set<String> queryDueFids(Long ts) {
		return getRedis().zRangeByScore(NS_FILE_IDS, 0, ts);
	}

	/**
	 * 清理过期fids
	 * 
	 * @param fids
	 * @return
	 */
	public static Long cleanDueFids(Set<String> fids) {
		return getRedis().zdel(NS_FILE_IDS,
				fids.toArray(new String[fids.size()]));
	}

}
