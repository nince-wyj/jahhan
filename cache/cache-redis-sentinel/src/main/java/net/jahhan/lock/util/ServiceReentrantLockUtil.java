package net.jahhan.lock.util;

import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.cache.context.RedisVariable;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.lock.DistributedLock;
import net.jahhan.lock.impl.ServiceReentrantLock;

import java.util.concurrent.TimeUnit;

/**
 * 服务重入锁
 * 
 * @author nince
 *
 */
public class ServiceReentrantLockUtil {

	private final static String PRE = BaseConfiguration.SERVICE + "_LOCK_";

	private final static int ttlSec = 5 * 60;

	private static String getKey(String lockName) {
		return PRE + lockName;
	}

	/**
	 * 服务锁
	 * 
	 * @param lockName
	 */
	public static DistributedLock lock(String lockName) {
		DistributedLock distributedLock = RedisVariable.getDBVariable().getServiceLock(getKey(lockName));
		if (null == distributedLock) {
			distributedLock = new ServiceReentrantLock(RedisConstants.COMMON, getKey(lockName), ttlSec);
			RedisVariable.getDBVariable().setServiceLock(getKey(lockName), distributedLock);
		}
		distributedLock.lock();
		return distributedLock;
	}

	/**
	 * 服务锁
	 * 
	 * @param lockName
	 */
	public static DistributedLock lock(String lockName, long ttl, TimeUnit timeUnit) {
		DistributedLock distributedLock = RedisVariable.getDBVariable().getServiceLock(getKey(lockName));
		if (null == distributedLock) {
			long tempttl=ttl;
			if(!timeUnit.equals(TimeUnit.MILLISECONDS)){
				tempttl=timeUnit.toMillis(ttl);
			}
			distributedLock = new ServiceReentrantLock(RedisConstants.COMMON, getKey(lockName), tempttl);
			RedisVariable.getDBVariable().setServiceLock(getKey(lockName), distributedLock);
		}
		distributedLock.lock();
		return distributedLock;
	}

	/**
	 * 服务锁释放
	 * 
	 * @param lockName
	 */
	public static void releaseLock(String lockName) {
		DistributedLock distributedLock = RedisVariable.getDBVariable().getServiceLock(getKey(lockName));
		distributedLock.unlock();
	}

}
