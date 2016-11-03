package net.jahhan.cache.repository.common;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisConstants;
import net.jahhan.cache.RedisFactory;
import net.jahhan.context.ApplicationContext;

/**
 * 分布式锁
 * 
 * @author nince
 *
 */
public class LockRepository {

	protected static Logger logger = LoggerFactory.getLogger(LockRepository.class);
	private final static String PRE = "LOCK:";

	private static Redis getRedis() {
		return RedisFactory.getRedis(RedisConstants.TABLE_COMMON, null);
	}

	private static String sha = "ae72f7d7318491f4e6fd0167aa73d6beccc73bc2";

	private final static int ttlSec = 5 * 60;

	private static String getKey(String lockName) {
		return PRE + lockName;
	}

	/**
	 * 加锁
	 * 
	 * @param lockName
	 */
	@SuppressWarnings("static-access")
	public static boolean lock(String lockName) {
		Redis redis = getRedis();
		String ret = null;
		int retryTime = 0;
		String lock = RandomStringUtils.randomAlphanumeric(8);
		ApplicationContext.CTX.getInvocationContext().setLock(lock);
		while ((null == ret || !(ret.equals("OK") || ret.equals("1"))) && retryTime < 3000) {
			try {
				Thread.currentThread().sleep(100);
				ret = redis.setNxTTL(getKey(lockName), lock, ttlSec);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
			retryTime++;
		}
		if ((ret.equals("OK") || ret.equals("1"))) {
			return true;
		}
		return false;
	}

	/**
	 * 加锁
	 * 
	 * @param lockName
	 */
	@SuppressWarnings("static-access")
	public static boolean lock(String lockName, int ttl) {
		Redis redis = getRedis();
		String ret = null;
		int retryTime = 0;
		String lock = RandomStringUtils.randomAlphanumeric(12);
		ApplicationContext.CTX.getInvocationContext().setLock(lock);
		while ((null == ret || !(ret.equals("OK") || ret.equals("1"))) && retryTime < 3000) {
			try {
				Thread.currentThread().sleep(100);
				ret = redis.setNxTTL(getKey(lockName), lock, ttl);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
			retryTime++;
		}
		if ((ret.equals("OK") || ret.equals("1"))) {
			return true;
		}
		return false;
	}

	/**
	 * 锁释放
	 * 
	 * @param lockName
	 */
	public static void releaseLock(String lockName) {
		Redis redis = getRedis();
		redis.evalsha(sha, 2, getKey(lockName), ApplicationContext.CTX.getInvocationContext().getLock());
	}
	
	public static void main(String[] args) {
		System.out.println(RandomStringUtils.randomAlphanumeric(12));
	}
}
