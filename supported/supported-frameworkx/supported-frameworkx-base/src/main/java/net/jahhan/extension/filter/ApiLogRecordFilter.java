package net.jahhan.extension.filter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.frameworkx.annotation.Activate;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.JsonUtil;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.content.UserOperationMessage;
import net.jahhan.spi.Filter;

@Activate(group = Constants.PROVIDER, order = -4000)
@Extension("apiLogRecordProvider")
@Singleton
public class ApiLogRecordFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws JahhanException {
		long startTime = System.currentTimeMillis();
		RpcContext context = RpcContext.getContext();
		Map<String, String> attachments = context.getAttachments();

		String interfaceClassName = invoker.getUrl().getParameter("interface");
		String methodName = invocation.getMethodName();

		Class<?> interfaceClass;
		Method interfaceMethod = null;
		try {
			interfaceClass = ApiLogRecordFilter.class.getClassLoader().loadClass(interfaceClassName);
			interfaceMethod = interfaceClass.getDeclaredMethod(methodName, invocation.getParameterTypes());
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
		}
		Parameter[] parameters = interfaceMethod.getParameters();
		Object[] arguments = invocation.getArguments();
		Map<String, Object> requestMap = new HashMap<>();
		for (int i = 0; i < parameters.length; i++) {
			requestMap.put(parameters[i].getName(), arguments[i]);
		}
		Result invoke = invoker.invoke(invocation);
		UserOperationMessage op = new UserOperationMessage(RpcContext.getContext().getRemoteHost(),
				interfaceClassName + "." + methodName, attachments, requestMap, invoke,
				System.currentTimeMillis() - startTime);
		LogUtil.requestInfo(JsonUtil.toJson(op));
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