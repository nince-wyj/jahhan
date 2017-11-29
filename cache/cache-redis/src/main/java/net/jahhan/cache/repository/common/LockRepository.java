package net.jahhan.cache.repository.common;

import java.util.UUID;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.cache.context.RedisVariable;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.exception.JahhanException;

/**
 * 分布式锁
 * 
 * @author nince
 *
 */
public class LockRepository {

	private final static String PRE = BaseConfiguration.SERVICE + "_LOCK:";

	private static Redis getGlobalRedis() {
		return RedisFactory.getRedis(RedisConstants.GLOBAL_LOCK, null);
	}

	private static Redis getRedis() {
		return RedisFactory.getRedis(RedisConstants.TABLE_CACHE, null);
	}

	private static String sha = "ae72f7d7318491f4e6fd0167aa73d6beccc73bc2";

	private final static int ttlSec = 5 * 60;

	private static String getKey(String lockName) {
		return PRE + lockName;
	}

	/**
	 * 服务锁
	 * 
	 * @param lockName
	 */
	public static boolean lock(String lockName) {
		return lock(lockName, ttlSec);
	}

	/**
	 * 全局锁
	 * 
	 * @param lockName
	 */
	public static boolean globalLock(String lockName) {
		return globalLock(lockName, ttlSec);
	}

	/**
	 * 服务锁
	 * 
	 * @param lockName
	 */
	public static boolean lock(String lockName, int ttl) {
		return lock(lockName, ttl, 3000, 100);
	}

	/**
	 * 全局锁
	 * 
	 * @param lockName
	 */
	public static boolean globalLock(String lockName, int ttl) {
		return globalLock(lockName, ttl, 3000, 100);
	}

	/**
	 * 服务锁
	 * 
	 * @param lockName
	 */
	@SuppressWarnings("static-access")
	public static boolean lock(String lockName, int ttl, int retryTime, long sleepTime) {
		Redis redis = getRedis();
		String ret = null;
		int retry = 0;
		String lock = UUID.randomUUID().toString();
		boolean setLock = RedisVariable.getDBVariable().setLock("s:" + lockName, lock);
		if (!setLock) {
			LogUtil.lockInfo("rget serviceLock:" + lockName + ",lock random:" + lock);
			return true;
		}
		while ((null == ret || !(ret.equals("OK") || ret.equals("1"))) && retry < retryTime) {
			try {
				Thread.currentThread().sleep(sleepTime);
				ret = redis.setNxTTL(getKey(lockName), lock, ttl);
			} catch (InterruptedException e) {
				LogUtil.error(e.getMessage(), e);
			}
			retry++;
		}
		if ((ret.equals("OK") || ret.equals("1"))) {
			LogUtil.lockInfo("get serviceLock:" + lockName + ",lock random:" + lock);
			return true;
		}
		return false;
	}

	/**
	 * 全局锁
	 * 
	 * @param lockName
	 */
	@SuppressWarnings("static-access")
	public static boolean globalLock(String lockName, int ttl, int retryTime, long sleepTime) {
		Redis redis = getGlobalRedis();
		String ret = null;
		int retry = 0;
		String lock = UUID.randomUUID().toString();
		boolean setLock = RedisVariable.getDBVariable().setLock("g:" + lockName, lock);
		if (!setLock) {
			LogUtil.lockInfo("rget globalLock:" + lockName + ",lock random:" + lock);
			return true;
		}
		while ((null == ret || !(ret.equals("OK") || ret.equals("1"))) && retry < retryTime) {
			try {
				Thread.currentThread().sleep(sleepTime);
				ret = redis.setNxTTL(lockName, lock, ttl);
			} catch (InterruptedException e) {
				LogUtil.error(e.getMessage(), e);
			}
			retry++;
		}
		if ((ret.equals("OK") || ret.equals("1"))) {
			LogUtil.lockInfo("get globalLock:" + lockName + ",lock random:" + lock);
			return true;
		}
		return false;
	}

	/**
	 * 服务锁释放
	 * 
	 * @param lockName
	 */
	public static void releaseLock(String lockName) {
		Redis redis = getRedis();
		String lock = RedisVariable.getDBVariable().getLock("s:" + lockName);
		int result = (int) redis.evalsha(sha, 2, getKey(lockName), lock);
		if (result == 1) {
			LogUtil.lockInfo("release serviceLock:" + lockName + ",lock random:" + lock);
		} else {
			LogUtil.lockInfo("releaseTimeout serviceLock:" + lockName + ",lock random:" + lock);
			JahhanException.throwException(JahhanErrorCode.LOCK_OVERTIME, "锁超时："+lockName);
		}
	}

	/**
	 * 全局锁释放
	 * 
	 * @param lockName
	 */
	public static void releaseGlobalLock(String lockName) {
		Redis redis = getGlobalRedis();
		String lock = RedisVariable.getDBVariable().getLock("g:" + lockName);
		int result = (int) redis.evalsha(sha, 2, getKey(lockName), lock);
		if (result == 1) {
			LogUtil.lockInfo("release globalLock:" + lockName + ",lock random:" + lock);
		} else {
			LogUtil.lockInfo("releaseTimeout globalLock:" + lockName + ",lock random:" + lock);
			JahhanException.throwException(JahhanErrorCode.LOCK_OVERTIME, "锁超时："+lockName);
		}
	}
}
