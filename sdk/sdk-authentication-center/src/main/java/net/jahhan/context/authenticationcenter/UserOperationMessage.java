package net.jahhan.context.authenticationcenter;

import java.util.Map;

import com.alibaba.dubbo.rpc.OperationMessage;
import com.alibaba.dubbo.rpc.Result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.service.context.AuthenticationVariable;
import net.jahhan.service.service.bean.Service;
import net.jahhan.service.service.bean.User;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserOperationMessage extends OperationMessage {

	private User user;
	private Service service;

	public UserOperationMessage(String remoteHost, String interfaceName, Map<String, String> attachments,
			Map<String, Object> requestMap, Result result, long usedMillisecond) {
		super(remoteHost, interfaceName, attachments, requestMap, result, null, usedMillisecond);
		AuthenticationVariable authenticationVariable = AuthenticationVariable.getAuthenticationVariable();
		this.user = authenticationVariable.getUser();
		this.service = authenticationVariable.getService();
	}
}