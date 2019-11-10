package net.jahhan.extension.filter;

import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.ThreadVariableContext;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.spring.aspect.Filter;
import net.jahhan.spring.aspect.Invocation;
import net.jahhan.spring.aspect.Invoker;
import net.jahhan.variable.BaseGlobalVariable;
import net.jahhan.variable.BaseThreadVariable;
import net.jahhan.variable.RequestVariable;

@Extension("variablecontext")
@Singleton
@Order(Integer.MIN_VALUE + 1)
public class VariableContextFilter implements Filter {

	public Object invoke(Invoker invoker, Invocation invocation) throws JahhanException {
		BaseContext applicationContext = BaseContext.CTX;
		ThreadVariableContext variableContext = new ThreadVariableContext();
		if (null == applicationContext.getThreadLocalUtil().getValue()) {
			applicationContext.getThreadLocalUtil().openThreadLocal(variableContext);
		}
		BaseThreadVariable base = (BaseThreadVariable) BaseThreadVariable.getThreadVariable("base");
		RequestVariable requestVariable = (RequestVariable) RequestVariable.getThreadVariable("request");
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
		((BaseGlobalVariable) BaseContext.CTX.getVariable("base")).setChain(chainId, Thread.currentThread());

		return invoker.invoke(invocation);
	}
}