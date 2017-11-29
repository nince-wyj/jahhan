package net.jahhan.globalTransaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaitingThreadHolder {
	protected static Map<String, Map<String, WaitingLock>> lockMap = new ConcurrentHashMap<>();
	protected static Map<String, WaitingLock> chainLockMap = new ConcurrentHashMap<>();

	public static void registWaitingThread(String lock, String chainId, long waitTime) {
		Map<String, WaitingLock> chainMap = lockMap.get(lock);
		if (null == chainMap) {
			chainMap = new ConcurrentHashMap<>();
			lockMap.put(lock, chainMap);
		}
		WaitingLock o = new WaitingLock();
		o.setLock(lock);
		o.setChain(chainId);
		chainLockMap.put(chainId, o);
		chainMap.put(chainId, o);
		synchronized (o) {
			try {
				o.wait(waitTime);
			} catch (InterruptedException e) {
			}
		}
	}

	public static void commit(String chainId) {
		WaitingLock waitingLock = chainLockMap.get(chainId);
		if (null != waitingLock) {
			String lockName = "GOBAL_TRANSACTION_" + chainId;
			String lock = waitingLock.getLock();
			if (lockName.equals(lock)) {
				Map<String, WaitingLock> map = lockMap.get(lock);
				map.remove(chainId);
				if (map.isEmpty()) {
					lockMap.remove(lock);
				}
				chainLockMap.remove(chainId);
				LockStatus chainLock = LockParamHolder.getChainLock(chainId);
				chainLock.setStatus(LockThreadStatus.WEAKUP);
				synchronized (waitingLock) {
					waitingLock.notify();
				}
			}
		}
	}

	public static void rollback(String chainId) {
		WaitingLock waitingLock = chainLockMap.get(chainId);
		if (null != waitingLock) {
			String lockName = "GOBAL_TRANSACTION_" + chainId;
			String lock = waitingLock.getLock();
			if (lockName.equals(lock)) {
				Map<String, WaitingLock> map = lockMap.get(lock);
				map.remove(chainId);
				if (map.isEmpty()) {
					lockMap.remove(lock);
				}
				chainLockMap.remove(chainId);
				LockStatus chainLock = LockParamHolder.getChainLock(chainId);
				chainLock.setStatus(LockThreadStatus.ERROR);
				synchronized (waitingLock) {
					waitingLock.notify();
				}
			}
		}

	}
}