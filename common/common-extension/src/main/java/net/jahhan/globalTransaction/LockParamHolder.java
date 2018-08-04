package net.jahhan.globalTransaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LockParamHolder {
	private static Map<String, LockStatus> lockMap = new ConcurrentHashMap<>();

	public static LockStatus newChainLock(String chain) {
		LockStatus lock = new LockStatus();
		lockMap.put(chain, lock);
		return lock;
	}

	public static LockThreadStatus getChainLockStatus(String chain) {
		LockStatus lockStatus = lockMap.get(chain);
		if (null != lockStatus) {
			return lockStatus.getStatus();
		}
		lockStatus = newChainLock(chain);
		return lockStatus.getStatus();
	}
	
	public static LockStatus getChainLock(String chain) {
		return lockMap.get(chain);
	}
	
	public static void removeChainLock(String chain){
		lockMap.remove(chain);
	}
}
