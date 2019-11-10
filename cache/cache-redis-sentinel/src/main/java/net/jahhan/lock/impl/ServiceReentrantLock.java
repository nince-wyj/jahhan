package net.jahhan.lock.impl;

import java.util.concurrent.TimeUnit;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.lock.DistributedLock;
import net.jahhan.variable.BaseThreadVariable;
import net.jahhan.variable.RedisVariable;

public class ServiceReentrantLock implements DistributedLock {
	private Redis redis;
	private int level = 0;
	private String lockName;
	/** 毫秒 */
	private long ttl = 0;

	public ServiceReentrantLock(String redisType, String lockName, long ttl) {
		this.redis = RedisFactory.getRedis(redisType, null);
		this.lockName = lockName;
		this.ttl = ttl;
	}

	static void selfInterrupt() {
		Thread.currentThread().interrupt();
	}

	@SuppressWarnings("static-access")
	@Override
	public void lock() {
		String ret = null;
		String requestId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getRequestId();
		if (level > 0) {
			LogUtil.lockInfo("reget serviceLock:" + lockName + ",lock request:" + requestId + ",level:" + level);
			level++;
			return;
		}
		if (ttl > 0) {
			ret = redis.setNxTTL(lockName, requestId, ttl, TimeUnit.MILLISECONDS);
		} else {
			ret = String.valueOf(redis.setnx(lockName, requestId));
		}
		if ("OK".equals(ret) || "1".equals(ret)) {
			LogUtil.lockInfo("get serviceLock:" + lockName + ",lock request:" + requestId + ",level:" + level);
			level++;
			return;
		}
		int count = 0;
		while (count < 20) {
			try {
				Thread.currentThread().sleep(ttl * 200);
			} catch (InterruptedException e) {
			}
			if (ttl > 0) {
				ret = redis.setNxTTL(lockName, requestId, ttl, TimeUnit.MILLISECONDS);
			} else {
				ret = String.valueOf(redis.setnx(lockName, requestId));
			}
			if ("OK".equals(ret) || "1".equals(ret)) {
				LogUtil.lockInfo("get serviceLock:" + lockName + ",lock request:" + requestId + ",level:" + level);
				level++;
				return;
			}
			count++;
		}

		JahhanException.throwException(JahhanErrorCode.UNKNOW_ERROR, "未知错误");
	}

	@Override
	public boolean tryLock() {
		Long ret = null;
		String requestId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getRequestId();
		if (level > 0) {
			LogUtil.lockInfo("reget serviceLock:" + lockName + ",lock request:" + requestId + ",level:" + level);
			level++;
			return true;
		}
		ret = redis.setnx(lockName, requestId);
		if (ret == 1) {
			LogUtil.lockInfo("get serviceLock:" + lockName + ",lock request:" + requestId + ",level:" + level);
			level++;
			return true;
		}
		return false;
	}

	@Override
	public void unlock() {
		level--;
		String requestId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getRequestId();
		LogUtil.lockInfo("unlock serviceLock:" + lockName + ",lock request:" + requestId + ",level:" + level);
		if (level == 0) {
			Long result = (Long) redis.releaseNoneReentrantLock(lockName, requestId);
			if (result == 1) {
				((RedisVariable) RedisVariable.getThreadVariable("redis")).removeServiceLock(lockName);
				LogUtil.lockInfo("release serviceLock:" + lockName + ",lock request:" + requestId + ",level:" + level);
			} else {
				LogUtil.lockInfo(
						"releaseTimeout serviceLock:" + lockName + ",lock request:" + requestId + ",level:" + level);
				JahhanException.throwException(JahhanErrorCode.LOCK_OVERTIME, "锁超时：" + lockName);
			}
		}

	}

	@Override
	public void close() throws Exception {
		unlock();
	}

}
