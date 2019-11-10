package net.jahhan.lock.impl;

import lombok.Data;
import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.globalTransaction.LockParamHolder;
import net.jahhan.globalTransaction.LockStatus;
import net.jahhan.globalTransaction.LockThreadStatus;
import net.jahhan.lock.DistributedLock;
import net.jahhan.lock.communication.LockWaitingThreadHolder;
import net.jahhan.variable.BaseThreadVariable;

@Data
public class GlobalReentrantLock implements DistributedLock {
	private Redis redis;
	private long level = 0;
	private String lockName;
	private int ttl = 0;

	public GlobalReentrantLock(String redisType, String lockName, int ttl) {
		this.redis = RedisFactory.getRedis(redisType, null);
		this.lockName = lockName;
		this.ttl = ttl;
	}

	@Override
	public void lock() {
		String chainId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
		level = redis.queueGetGlobalReentrantLock(lockName, chainId, level, ttl);
		if (level > 0) {
			LogUtil.lockInfo("get globalLock:" + lockName + ",lock chain:" + chainId + ",level:" + level);
			return;
		}
		if (level < 0) {
			JahhanException.throwException(JahhanErrorCode.LOCK_ERROE, "锁层级错误");
		}
		LockParamHolder.newChainLock(chainId);
		int count = 0;
		while (count < 1000) {
			LockWaitingThreadHolder.registWaitingThread(lockName, chainId, ttl * 1000);
			LockThreadStatus chainLockStatus = LockParamHolder.getChainLockStatus(chainId);
			switch (chainLockStatus) {
			case WEAKUP: {
				if (weakup()) {
					return;
				}
				break;
			}
			case COMPETE: {
				if (compete()) {
					return;
				}
				break;
			}
			case REQUEUE: {
				if (tryLock()) {
					chainLockStatus = LockThreadStatus.BLOCK;
					return;
				}
				break;
			}
			default:
				break;
			}
			count++;
		}
		JahhanException.throwException(JahhanErrorCode.LOCK_ERROE, "锁超重试");
	}

	@Override
	public boolean tryLock() {
		String chainId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
		level = redis.queueGetGlobalReentrantLock(lockName, chainId, level, ttl);
		if (level > 0) {
			LogUtil.lockInfo("get globalLock:" + lockName + ",lock chain:" + chainId + ",level:" + level);
			return true;
		}
		return false;
	}

	@Override
	public void unlock() {
		String chainId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
		level = redis.unLockGlobalReentrantLock(lockName, chainId, level);
		LockParamHolder.removeChainLock(chainId);
		LogUtil.lockInfo("release globalLock:" + lockName + ",lock chain:" + chainId + ",level:" + level);
		if (level < 0) {
			LogUtil.lockInfo("releaseError globalLock:" + lockName + ",lock chain:" + chainId + ",level:" + level);
			JahhanException.throwException(JahhanErrorCode.LOCK_ERROE, "锁错误：" + lockName);
		}
	}

	private boolean weakup() {
		String chainId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
		LockStatus chainLock = LockParamHolder.getChainLock(chainId);
		level = redis.callGetGlobalReentrantLock(lockName, chainId, ttl);
		chainLock.setStatus(LockThreadStatus.BLOCK);
		if (level > 0) {
			LogUtil.lockInfo("get globalLock:" + lockName + ",lock chain:" + chainId + ",level:" + level);
			return true;
		}
		return false;
	}

	private boolean compete() {
		String chainId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
		LockStatus chainLock = LockParamHolder.getChainLock(chainId);
		level = redis.competeGetGlobalReentrantLock(lockName, chainId, chainLock.getKey(), ttl);
		chainLock.setStatus(LockThreadStatus.BLOCK);
		if (level > 0) {
			LogUtil.lockInfo("get globalLock:" + lockName + ",lock chain:" + chainId + ",level:" + level);
			return true;
		}
		return false;
	}

	@Override
	public void close() throws Exception {
		unlock();
	}

}
