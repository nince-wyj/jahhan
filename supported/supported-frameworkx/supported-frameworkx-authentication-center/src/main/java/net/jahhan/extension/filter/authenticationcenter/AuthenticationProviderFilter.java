package net.jahhan.extension.filter.authenticationcenter;

import java.lang.reflect.Method;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.frameworkx.annotation.Activate;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.exception.JahhanException;
import net.jahhan.sdk.authenticationcenter.annotation.FirstToken;
import net.jahhan.sdk.authenticationcenter.annotation.NoneToken;
import net.jahhan.sdk.authenticationcenter.annotation.RequestMode;
import net.jahhan.sdk.authenticationcenter.annotation.ServiceAuthentication;
import net.jahhan.sdk.authenticationcenter.annotation.UserAuthentication;
import net.jahhan.sdk.authenticationcenter.constant.RequestModeType;
import net.jahhan.service.context.AuthenticationVariable;
import net.jahhan.service.service.bean.Service;
import net.jahhan.service.service.bean.User;
import net.jahhan.spi.Filter;

@Activate(group = Constants.PROVIDER, order = 1500)
@Extension("authenticationProvider")
@Singleton
public class AuthenticationProviderFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws JahhanException {
		RpcContext context = RpcContext.getContext();
		Map<String, String> attachments = context.getAttachments();
		HttpServletRequest request = context.getRequest(HttpServletRequest.class);
		HttpServletResponse response = context.getResponse(HttpServletResponse.class);

		String interfaceClassName = invoker.getUrl().getParameter("interface");
		String implClassName = invoker.getUrl().getParameter("class");
		String methodName = invocation.getMethodName();

		Class<?> interfaceClass;
		Method interfaceMethod = null;
		Method implMethod = null;
		try {
			interfaceClass = AuthenticationProviderFilter.class.getClassLoader().loadClass(interfaceClassName);
			implMethod = Class.forName(implClassName).getDeclaredMethod(methodName,
					invocation.getParameterTypes());
			interfaceMethod = interfaceClass.getDeclaredMethod(methodName, invocation.getParameterTypes());
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
		}
		AuthenticationVariable authenticationVariable = AuthenticationVariable.getAuthenticationVariable();
		User user = authenticationVariable.getUser();
		Service service = authenticationVariable.getService();
		if (null != request && null != response) {
			String ipAddr = getIpAddr(request);
			attachments.put("client_ip", ipAddr);

			RequestMode requestMode = interfaceMethod.getAnnotation(RequestMode.class);
			RequestModeType defineType = RequestModeType.COMMON;
			if (null != requestMode) {
				defineType = requestMode.value();
			}
			RequestModeType requestType = RequestModeType.COMMON;
			if (!authenticationVariable.isCommonRequest()) {
				requestType = RequestModeType.LOGIN;
			}
			Assert.isTrue(defineType.equals(requestType), "接口定义与请求不符合！", HttpStatus.SC_BAD_REQUEST,
					JahhanErrorCode.PARAMETER_ERROR);

			UserAuthentication userAuthentication = interfaceMethod.getAnnotation(UserAuthentication.class);
			
			if (null != userAuthentication) {
				Assert.notNull(user, "用户未登陆！", HttpStatus.SC_BAD_REQUEST, JahhanErrorCode.NO_AUTHORITY);
			}
			ServiceAuthentication serviceAuthentication = interfaceMethod.getAnnotation(ServiceAuthentication.class);
			if (null != serviceAuthentication) {
				Assert.notNull(service, "服务未登陆！", HttpStatus.SC_BAD_REQUEST, JahhanErrorCode.NO_AUTHORITY);
			}
			if (!authenticationVariable.isCrypt()) {
				Assert.isTrue(
						BaseConfiguration.IS_DEBUG
								|| (null == userAuthentication && null == serviceAuthentication)
								|| (null != authenticationVariable.getService()
										&& authenticationVariable.getService().isInnerService()),
						"接口定义与请求不符合！", HttpStatus.SC_BAD_REQUEST, JahhanErrorCode.NO_AUTHORITY);
			}
			NoneToken noneToken = implMethod.getAnnotation(NoneToken.class);
			if (authenticationVariable.isNoneToken() && !authenticationVariable.isDocRequest() && null == noneToken) {
				Assert.isTrue(null == noneToken, "无token错误！！", HttpStatus.SC_BAD_REQUEST, JahhanErrorCode.NO_AUTHORITY);
			}
			if (authenticationVariable.isFirstSingleToken() && !BaseConfiguration.IS_DEBUG && null == noneToken) {
				FirstToken firstToken = implMethod.getAnnotation(FirstToken.class);
				Assert.notNull(firstToken, "鉴权失败！", HttpStatus.SC_UNAUTHORIZED, JahhanErrorCode.NO_AUTHORITY);
			} else {
				if (!authenticationVariable.isDocRequest() && !BaseConfiguration.IS_DEBUG && null == noneToken) {

					Assert.isTrue(null != user || null != service, "无token错误！", HttpStatus.SC_BAD_REQUEST,
							JahhanErrorCode.NO_AUTHORITY);
				}
			}
		}
		Result invoke = invoker.invoke(invocation);
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