package net.jahhan.variable;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.common.extension.annotation.ThreadVariable;
import net.jahhan.common.extension.context.Variable;

@Data
@EqualsAndHashCode(callSuper = false)
@ThreadVariable("request")
public class RequestVariable extends Variable {
	private InetSocketAddress remoteAddress;
	private final Map<String, String> attachments = new HashMap<String, String>();
	private Object request;
	private Object response;

	public RequestVariable setAttachment(String key, String value) {
		if (value == null) {
			attachments.remove(key);
		} else {
			attachments.put(key, value);
		}
		return this;
	}

	public RequestVariable setRemoteAddress(String host, int port) {
		if (port < 0) {
			port = 0;
		}
		this.remoteAddress = InetSocketAddress.createUnresolved(host, port);
		return this;
	}

	public String getRemoteHost() {
		return remoteAddress.getHostName();
	}

}
