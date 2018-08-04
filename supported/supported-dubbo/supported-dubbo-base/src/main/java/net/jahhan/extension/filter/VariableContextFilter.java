package net.jahhan.extension.filter;

import java.util.Map;
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

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.BaseVariable;
import net.jahhan.common.extension.context.VariableContext;

@Activate(group = Constants.PROVIDER, order = Integer.MIN_VALUE + 1)
@Extension("variablecontext")
@Singleton
public class VariableContextFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		BaseContext applicationContext = BaseContext.CTX;
		VariableContext variableContext = new VariableContext();
		if (null == applicationContext.getThreadLocalUtil().getValue()) {
			applicationContext.getThreadLocalUtil().openThreadLocal(variableContext);
		}
		BaseVariable base = BaseVariable.getBaseVariable();
		Map<String, String> attachments = RpcContext.getContext().getAttachments();
		String requestId = attachments.get("request_id");
		String chainId = attachments.get("chain_id");
		String behaviorId = attachments.get("behavior_id");
		if (null == requestId) {
			requestId = UUID.randomUUID().toString();
			attachments.put("request_id", requestId);
		}
		if (null == chainId) {
			chainId = UUID.randomUUID().toString();
			attachments.put("chain_id", chainId);
		}
		if (null == behaviorId) {
			behaviorId = UUID.randomUUID().toString();
			attachments.put("behavior_id", behaviorId);
		}
		base.setRequestId(requestId);
		base.setChainId(chainId);
		base.setBehaviorId(behaviorId);
		BaseContext.CTX.setChain(chainId, Thread.currentThread());

		Result invoke = invoker.invoke(invocation);
		// BaseContext.CTX.removeChain(chainId);
		return invoke;

	}
}