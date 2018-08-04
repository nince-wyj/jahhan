package net.jahhan.extension.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.frameworkx.annotation.Activate;

import net.jahhan.cache.context.RedisVariable;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.JsonUtil;
import net.jahhan.lock.impl.GlobalReentrantLock;
import net.jahhan.spi.Filter;

@Activate(group = Constants.CONSUMER, order = -9000)
@Extension("globalLockconsumercontext")
@Singleton
public class GlobalLockConsumerContextFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws JahhanException {
		Map<String, GlobalReentrantLock> globalLockMap = RedisVariable.getDBVariable().getGlobalLockMap();
		if (null != globalLockMap) {
			Set<String> keySet = globalLockMap.keySet();
			Map<String, Long> globalLockLevelMap = new HashMap<>();
			for (String key : keySet) {
				GlobalReentrantLock globalReentrantLock = globalLockMap.get(key);
				globalLockLevelMap.put(key, globalReentrantLock.getLevel());
			}
			RpcContext.getContext().setAttachment("global_locks", JsonUtil.toJson(globalLockLevelMap).replace(",", "$|"));
		}
		return invoker.invoke(invocation);
	}
}