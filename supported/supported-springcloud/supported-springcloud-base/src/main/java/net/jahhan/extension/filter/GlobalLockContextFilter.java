package net.jahhan.extension.filter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.annotation.GlobalSyncTransaction;
import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.JsonUtil;
import net.jahhan.lock.impl.GlobalReentrantLock;
import net.jahhan.lock.util.GlobalReentrantLockUtil;
import net.jahhan.spring.aspect.Filter;
import net.jahhan.spring.aspect.Invocation;
import net.jahhan.spring.aspect.Invoker;
import net.jahhan.variable.BaseThreadVariable;
import net.jahhan.variable.RedisVariable;
import net.jahhan.variable.RequestVariable;

@Singleton
@Order(-5000)
public class GlobalLockContextFilter implements Filter {

	@SuppressWarnings("unchecked")
	public Object invoke(Invoker invoker, Invocation invocation) throws JahhanException {
		Object invoke = null;
		RequestVariable requestVariable = (RequestVariable) RequestVariable.getThreadVariable("request");
		String globalLocks = requestVariable.getAttachments().get("global_locks");
		Map<String, GlobalReentrantLock> globalLockMap = new HashMap<>();
		if (null != globalLocks) {
			globalLocks = globalLocks.replace("$|", ",");
			Map<String, Long> globalLockLevelMap = JsonUtil.fromJson(globalLocks, Map.class);

			Set<String> keySet = globalLockLevelMap.keySet();
			for (String key : keySet) {
				GlobalReentrantLock globalReentrantLock = new GlobalReentrantLock(RedisConstants.GLOBAL_LOCK, key, 300);
				globalReentrantLock.setLevel(((Number) globalLockLevelMap.get(key)).longValue());
				globalLockMap.put(key, globalReentrantLock);
			}
			((RedisVariable) RedisVariable.getThreadVariable("redis")).setGlobalLockMap(globalLockMap);
		}
		String transactionLock = "GlobalTransaction_" + ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
		if (globalLockMap.containsKey(GlobalReentrantLockUtil.getPRE() + transactionLock)) {
			((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).setDbLazyCommit(true);
		}
		Method method = invocation.getMethod();
		GlobalSyncTransaction globalSyncTransaction = method.getAnnotation(GlobalSyncTransaction.class);
		GlobalReentrantLock lock = null;
		if (null != globalSyncTransaction) {
			lock = GlobalReentrantLockUtil.lock(transactionLock);
		}
		try {
			invoke = invoker.invoke(invocation);
			if (null != globalSyncTransaction) {
				GlobalReentrantLockUtil.releaseLock(lock);
			}
			if (null != globalLockMap) {
				Set<String> keySet = globalLockMap.keySet();
				Map<String, Long> globalLockLevelMap = new HashMap<>();
				for (String key : keySet) {
					GlobalReentrantLock globalReentrantLock = globalLockMap.get(key);
					globalLockLevelMap.put(key, globalReentrantLock.getLevel());
				}
				requestVariable.setAttachment("global_locks", JsonUtil.toJson(globalLockLevelMap).replace(",", "$|"));
			}
		} catch (Exception e) {
			if (null != globalSyncTransaction) {
				GlobalReentrantLockUtil.releaseChainLock();
			}
			throw e;
		}
		return invoke;
	}
}