package net.jahhan.lock.util;

import lombok.Getter;
import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.lock.impl.GlobalReentrantLock;
import net.jahhan.variable.BaseThreadVariable;
import net.jahhan.variable.RedisVariable;

/**
 * 全局同步重入锁
 * 
 * @author nince
 *
 */
public class GlobalReentrantLockUtil {
	@Getter
	private final static String PRE = "GLOBAL_LOCK_";

	private final static int ttlSec = 5 * 60;

	private static String getKey(String lockName) {
		return PRE + lockName;
	}

	public static GlobalReentrantLock lock(String lockName) {
		GlobalReentrantLock distributedLock = (GlobalReentrantLock) ((RedisVariable) RedisVariable.getThreadVariable("redis"))
				.getGlobalLock(getKey(lockName));
		if (null == distributedLock) {
			distributedLock = new GlobalReentrantLock(RedisConstants.GLOBAL_LOCK, getKey(lockName), ttlSec);
			((RedisVariable) RedisVariable.getThreadVariable("redis")).setGlobalLock(getKey(lockName), distributedLock);
		}
		distributedLock.lock();
		return distributedLock;
	}

	public static GlobalReentrantLock lock(String lockName, int ttl) {
		GlobalReentrantLock distributedLock = (GlobalReentrantLock) ((RedisVariable) RedisVariable.getThreadVariable("redis"))
				.getGlobalLock(getKey(lockName));
		if (null == distributedLock) {
			distributedLock = new GlobalReentrantLock(RedisConstants.GLOBAL_LOCK, getKey(lockName), ttl);
			((RedisVariable) RedisVariable.getThreadVariable("redis")).setGlobalLock(getKey(lockName), distributedLock);
		}
		distributedLock.lock();
		return distributedLock;
	}

	public static void releaseLock(GlobalReentrantLock distributedLock) {
		distributedLock.unlock();
		if (distributedLock.getLevel() == 0) {
			((RedisVariable) RedisVariable.getThreadVariable("redis"))
					.removeGlobalLock(getKey(distributedLock.getLockName()));
		}
	}

	public static void releaseLock(String lockName) {
		GlobalReentrantLock distributedLock = (GlobalReentrantLock) ((RedisVariable) RedisVariable.getThreadVariable("redis"))
				.getGlobalLock(getKey(lockName));
		releaseLock(distributedLock);
	}

	public static void releaseLock(String lockName, int level) {
		GlobalReentrantLock distributedLock = (GlobalReentrantLock) ((RedisVariable) RedisVariable.getThreadVariable("redis"))
				.getGlobalLock(getKey(lockName));
		if (null != distributedLock && level == distributedLock.getLevel()) {
			distributedLock.unlock();
			if (distributedLock.getLevel() == 0) {
				((RedisVariable) RedisVariable.getThreadVariable("redis"))
						.removeGlobalLock(getKey(distributedLock.getLockName()));
			}
		} else {
			String chainId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
			LogUtil.lockInfo("releaseError globalLock:" + lockName + ",lock chain:" + chainId + ",level:" + level);
			JahhanException.throwException(JahhanErrorCode.LOCK_ERROE, "锁错误：" + lockName);
		}
	}

	public static void releaseChainLock() {
		Redis redis = RedisFactory.getRedis(RedisConstants.GLOBAL_LOCK, null);
		String chainId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
		redis.releaseChainLock(chainId);
		LogUtil.lockInfo("releaseChain globalLock:all,lock chain:" + chainId + ",level:0");
	}
}
