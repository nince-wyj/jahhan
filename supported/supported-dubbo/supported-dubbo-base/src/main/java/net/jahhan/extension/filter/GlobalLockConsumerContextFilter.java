package net.jahhan.extension.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;

import net.jahhan.cache.context.RedisVariable;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.utils.JsonUtil;
import net.jahhan.context.BaseVariable;
import net.jahhan.lock.impl.GlobalReentrantLock;

@Activate(group = Constants.CONSUMER, order = -9000)
@Extension("globalLockconsumercontext")
@Singleton
public class GlobalLockConsumerContextFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
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
		RpcContext.getContext().setAttachment("request_id", UUID.randomUUID().toString());
		RpcContext.getContext().setAttachment("chain_id", BaseVariable.getBaseVariable().getChainId());
		return invoker.invoke(invocation);
	}
}