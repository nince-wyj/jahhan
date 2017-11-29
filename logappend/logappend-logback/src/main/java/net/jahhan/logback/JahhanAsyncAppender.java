package net.jahhan.logback;

import java.util.Map;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import net.jahhan.context.BaseContext;
import net.jahhan.context.BaseVariable;
import net.jahhan.context.Node;
import net.jahhan.context.VariableContext;

public class JahhanAsyncAppender extends AsyncAppender {
	@Override
	protected void append(ILoggingEvent eventObject) {
		JahhanLoggingEvent frameWorkXLoggingEvent = new JahhanLoggingEvent(eventObject);
		frameWorkXLoggingEvent.setThreadId(String.valueOf(Thread.currentThread().getId()));
		VariableContext variableContext = BaseContext.CTX.getVariableContext();
		String requestId = "";
		String chainId = "";
		if (null != variableContext) {
			BaseVariable base = BaseVariable.getBaseVariable();
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
