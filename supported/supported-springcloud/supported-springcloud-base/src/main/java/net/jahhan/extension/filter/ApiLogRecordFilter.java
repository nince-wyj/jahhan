package net.jahhan.extension.filter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.utils.JsonUtil;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.content.UserOperationMessage;
import net.jahhan.exception.JahhanException;
import net.jahhan.request.context.RequestVariable;
import net.jahhan.spring.aspect.Filter;
import net.jahhan.spring.aspect.Invocation;
import net.jahhan.spring.aspect.Invoker;

@Singleton
@Order(-4000)
public class ApiLogRecordFilter implements Filter {

	public Object invoke(Invoker invoker, Invocation invocation) throws JahhanException {
		long startTime = System.currentTimeMillis();
		RequestVariable requestVariable = RequestVariable.getVariable();
		Map<String, String> attachments = requestVariable.getAttachments();

		Method method = invocation.getMethod();
		Parameter[] parameters = method.getParameters();
		Object[] arguments = invocation.getArguments();
		Map<String, Object> requestMap = new HashMap<>();
		for (int i = 0; i < parameters.length; i++) {
			requestMap.put(parameters[i].getName(), arguments[i]);
		}
		Object invoke = null;
		try {
			invoke = invoker.invoke(invocation);
			UserOperationMessage op = new UserOperationMessage(requestVariable.getRemoteHost(),
					invocation.getProceedingJoinPoint().getTarget().getClass().getName() + "." + method.getName(),
					attachments, requestMap, invoke, null, System.currentTimeMillis() - startTime);
			LogUtil.requestInfo(JsonUtil.toJson(op));
		} catch (Exception e) {
			UserOperationMessage op = new UserOperationMessage(requestVariable.getRemoteHost(),
					invocation.getProceedingJoinPoint().getTarget().getClass().getName() + "." + method.getName(),
					attachments, requestMap, invoke, e.getMessage(), System.currentTimeMillis() - startTime);
			LogUtil.requestInfo(JsonUtil.toJson(op));
			throw e;
		}
		return invoke;
	}

	public String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		String[] ips = ip.split(",");
		for (int i = 0; i < ips.length; i++) {
			if (!ips[i].equalsIgnoreCase("unknown")) {
				ip = ips[i];
				break;
			}
		}
		return ip;
	}
}