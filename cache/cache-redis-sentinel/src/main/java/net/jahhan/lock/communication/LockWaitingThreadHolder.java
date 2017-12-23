package net.jahhan.lock.communication;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.jahhan.globalTransaction.LockParamHolder;
import net.jahhan.globalTransaction.LockStatus;
import net.jahhan.globalTransaction.LockThreadStatus;
import net.jahhan.globalTransaction.WaitingLock;

public class LockWaitingThreadHolder {
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
	public static void compete(String lock, String key) {
		Map<String, WaitingLock> chainMap = lockMap.get(lock);
		if (null != chainMap) {
			Set<String> keySet = chainMap.keySet();
			for (String chain : keySet) {
				WaitingLock waitingLock = chainMap.get(chain);
				if (null != waitingLock) {
					LockStatus chainLock = LockParamHolder.getChainLock(chain);
					chainLock.setStatus(LockThreadStatus.COMPETE);
					chainLock.setKey(key);
					synchronized (waitingLock) {
						waitingLock.notify();
					}
					chainMap.remove(chain);
					chainLockMap.remove(chain);
				}
			}
			lockMap.remove(lock);
		}
	}

	public static void weakUp(String chainId, String lockName) {
		WaitingLock waitingLock = chainLockMap.get(chainId);
		if (null != waitingLock) {
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
}