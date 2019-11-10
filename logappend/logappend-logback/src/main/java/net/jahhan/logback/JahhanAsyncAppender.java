package net.jahhan.logback;

import java.util.Map;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.Node;
import net.jahhan.common.extension.context.ThreadVariableContext;
import net.jahhan.variable.BaseThreadVariable;

public class JahhanAsyncAppender extends AsyncAppender {
	@Override
	protected void append(ILoggingEvent eventObject) {
		JahhanLoggingEvent frameWorkXLoggingEvent = new JahhanLoggingEvent(eventObject);
		frameWorkXLoggingEvent.setThreadId(String.valueOf(Thread.currentThread().getId()));
		ThreadVariableContext variableContext = BaseContext.CTX.getVariableContext();
		String requestId = "";
		String chainId = "";
		if (null != variableContext) {
			BaseThreadVariable base = (BaseThreadVariable) BaseThreadVariable.getThreadVariable("base");
			if (null != base) {
				requestId = base.getRequestId();
				chainId=base.getChainId();
			}
		}
		Node node = BaseContext.CTX.getNode();
		Map<String, Integer> ports = node.getPorts();
		frameWorkXLoggingEvent.setRequestId(requestId);
		frameWorkXLoggingEvent.setChainId(chainId);
		frameWorkXLoggingEvent.setPid(node.getPid());
		frameWorkXLoggingEvent.setPort(ports.get("http"));
		super.append(frameWorkXLoggingEvent);
	}
}
