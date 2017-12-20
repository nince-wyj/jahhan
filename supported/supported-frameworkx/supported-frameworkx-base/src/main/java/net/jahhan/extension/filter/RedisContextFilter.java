package net.jahhan.extension.filter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcResult;
import com.frameworkx.annotation.Activate;

import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.cache.context.RedisVariable;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.annotation.GlobalSyncTransaction;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.JsonUtil;
import net.jahhan.context.BaseVariable;
import net.jahhan.exception.JahhanException;
import net.jahhan.lock.impl.GlobalReentrantLock;
import net.jahhan.lock.util.GlobalReentrantLockUtil;
import net.jahhan.spi.Filter;

@Activate(group = Constants.PROVIDER, order = -5000)
@Extension("rediscontext")
@Singleton
public class RedisContextFilter implements Filter {

	@SuppressWarnings("unchecked")
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws JahhanException {
		Result invoke = null;
		try {
			String globalLocks = RpcContext.getContext().getAttachments().get("global_locks");
			Map<String, GlobalReentrantLock> globalLockMap = new HashMap<>();
			if (null != globalLocks) {
				globalLocks = globalLocks.replace("$|", ",");
				Map<String, Long> globalLockLevelMap = JsonUtil.fromJson(globalLocks, Map.class);

				Set<String> keySet = globalLockLevelMap.keySet();
				for (String key : keySet) {
					GlobalReentrantLock globalReentrantLock = new GlobalReentrantLock(RedisConstants.GLOBAL_LOCK, key,
							300);
					globalReentrantLock.setLevel(((Number) globalLockLevelMap.get(key)).longValue());
					globalLockMap.put(key, globalReentrantLock);
				}
				RedisVariable.getDBVariable().setGlobalLockMap(globalLockMap);
			}
			String transactionLock = "GlobalTransaction_" + BaseVariable.getBaseVariable().getChainId();
			if (globalLockMap.containsKey(GlobalReentrantLockUtil.getPRE() + transactionLock)) {
				BaseVariable.getBaseVariable().setDbLazyCommit(true);
			}
			String implClassName = invoker.getUrl().getParameter("class");
			String methodName = invocation.getMethodName();
			Method method = null;
			try {
				method = Class.forName(implClassName).getDeclaredMethod(methodName,
						invocation.getParameterTypes());
			} catch (NoSuchMethodException | SecurityException e) {
				throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
			}
			GlobalSyncTransaction globalSyncTransaction = method.getAnnotation(GlobalSyncTransaction.class);
			GlobalReentrantLock lock = null;

			if (null != globalSyncTransaction) {
				lock = GlobalReentrantLockUtil.lock(transactionLock);
			}

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
				RpcContext.getContext().setAttachment("global_locks",
						JsonUtil.toJson(globalLockLevelMap).replace(",", "$|"));
			}
			Throwable exception = invoke.getException();
			if (null != exception) {
				GlobalReentrantLockUtil.releaseChainLock();
			}
		} catch (Exception e) {
			invoke = new RpcResult(e);
		}
		return invoke;
	}
}