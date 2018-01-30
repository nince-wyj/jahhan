package net.jahhan.content;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.context.OperationMessage;
import net.jahhan.service.context.AuthenticationVariable;
import net.jahhan.service.service.bean.Service;
import net.jahhan.service.service.bean.User;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserOperationMessage extends OperationMessage {

	private User user;
	private Service service;

	public UserOperationMessage(String remoteHost, String interfaceName, Map<String, String> attachments,
			Map<String, Object> requestMap, Object result, String errorMessage, long usedMillisecond) {
		this.remoteHost = remoteHost;
		this.interfaceName = interfaceName;
		this.usedMillisecond = usedMillisecond;
		this.attachments = attachments;
		this.requestMap = requestMap;
		this.result = result;
		this.exceptionMsg = errorMessage;
		AuthenticationVariable authenticationVariable = AuthenticationVariable.getAuthenticationVariable();
		this.user = authenticationVariable.getUser();
		this.service = authenticationVariable.getService();
	}

	public UserOperationMessage(String remoteHost, String interfaceName, Map<String, String> attachments,
			Map<String, Object> requestMap, Object result, long usedMillisecond) {
		this(remoteHost, interfaceName, attachments, requestMap, result, null, usedMillisecond);
	}
}