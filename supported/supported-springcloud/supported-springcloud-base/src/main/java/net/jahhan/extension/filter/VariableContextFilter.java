package net.jahhan.extension.filter;

import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.annotation.Order;
import net.jahhan.context.BaseContext;
import net.jahhan.context.BaseVariable;
import net.jahhan.context.VariableContext;
import net.jahhan.exception.JahhanException;
import net.jahhan.request.context.RequestVariable;
import net.jahhan.spring.aspect.Filter;
import net.jahhan.spring.aspect.Invocation;
import net.jahhan.spring.aspect.Invoker;

@Extension("variablecontext")
@Singleton
@Order(Integer.MIN_VALUE + 1)
public class VariableContextFilter implements Filter {

	public Object invoke(Invoker invoker, Invocation invocation) throws JahhanException {
		BaseContext applicationContext = BaseContext.CTX;
		VariableContext variableContext = new VariableContext();
		if (null == applicationContext.getThreadLocalUtil().getValue()) {
			applicationContext.getThreadLocalUtil().openThreadLocal(variableContext);
		}
		BaseVariable base = BaseVariable.getBaseVariable();
		RequestVariable requestVariable = RequestVariable.getVariable();
		Map<String, String> attachments = requestVariable.getAttachments();
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

		return invoker.invoke(invocation);
	}
}