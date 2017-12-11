package net.jahhan.extension.filter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.frameworkx.annotation.Activate;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.JsonUtil;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.content.UserOperationMessage;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.Filter;

@Activate(group = Constants.PROVIDER)
@Extension("exception")
@Singleton
public class ExceptionFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws JahhanException {
		long startTime = System.currentTimeMillis();
		try {
			Result result = invoker.invoke(invocation);
			if (result.hasException() && GenericService.class != invoker.getInterface()) {
				try {
					Throwable exception = result.getException();

					if (exception instanceof JahhanException) {
						RpcContext context = RpcContext.getContext();
						Map<String, String> attachments = context.getAttachments();

						String interfaceClassName = invoker.getUrl().getParameter("interface");
						String methodName = invocation.getMethodName();

						Class<?> interfaceClass;
						Method method = null;
						try {
							interfaceClass = ExceptionFilter.class.getClassLoader().loadClass(interfaceClassName);
							method = interfaceClass.getDeclaredMethod(methodName, invocation.getParameterTypes());
						} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
							throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
						}
						Parameter[] parameters = method.getParameters();
						Object[] arguments = invocation.getArguments();
						Map<String, Object> requestMap = new HashMap<>();
						for (int i = 0; i < parameters.length; i++) {
							requestMap.put(parameters[i].getName(), arguments[i]);
						}

						UserOperationMessage op = new UserOperationMessage(RpcContext.getContext().getRemoteHost(),
								invoker.getInterface().getName() + "." + invocation.getMethodName(), attachments,
								requestMap, result, null, System.currentTimeMillis() - startTime);
						StringBuilder sb = new StringBuilder();
						sb.append("operationMessage:").append(JsonUtil.toJson(op)).append(",exception message:");
						LogUtil.requestError(sb.toString(), exception);
						return result;
					}
					return new RpcResult(new JahhanException(exception));
				} catch (Throwable e) {
					RpcContext context = RpcContext.getContext();
					Map<String, String> attachments = context.getAttachments();

					String interfaceClassName = invoker.getUrl().getParameter("interface");
					String methodName = invocation.getMethodName();

					Class<?> interfaceClass;
					Method method = null;
					try {
						interfaceClass = ExceptionFilter.class.getClassLoader().loadClass(interfaceClassName);
						method = interfaceClass.getDeclaredMethod(methodName, invocation.getParameterTypes());
					} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e2) {
						throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e2);
					}
					Parameter[] parameters = method.getParameters();
					Object[] arguments = invocation.getArguments();
					Map<String, Object> requestMap = new HashMap<>();
					for (int i = 0; i < parameters.length; i++) {
						requestMap.put(parameters[i].getName(), arguments[i]);
					}

					UserOperationMessage op = new UserOperationMessage(RpcContext.getContext().getRemoteHost(),
							invoker.getInterface().getName() + "." + invocation.getMethodName(), attachments,
							requestMap, result, null, System.currentTimeMillis() - startTime);
					StringBuilder sb = new StringBuilder();
					sb.append("operationMessage:").append(JsonUtil.toJson(op)).append(",exception message:");
					LogUtil.requestError(sb.toString(), e);
					return new RpcResult(new JahhanException(result.getException()));
				}
			}
			return result;
		} catch (RuntimeException e) {
			RpcContext context = RpcContext.getContext();
			Map<String, String> attachments = context.getAttachments();

			String interfaceClassName = invoker.getUrl().getParameter("interface");
			String methodName = invocation.getMethodName();

			Class<?> interfaceClass;
			Method method = null;
			try {
				interfaceClass = ExceptionFilter.class.getClassLoader().loadClass(interfaceClassName);
				method = interfaceClass.getDeclaredMethod(methodName, invocation.getParameterTypes());
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e2) {
				throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e2);
			}
			Parameter[] parameters = method.getParameters();
			Object[] arguments = invocation.getArguments();
			Map<String, Object> requestMap = new HashMap<>();
			for (int i = 0; i < parameters.length; i++) {
				requestMap.put(parameters[i].getName(), arguments[i]);
			}

			UserOperationMessage op = new UserOperationMessage(RpcContext.getContext().getRemoteHost(),
					invoker.getInterface().getName() + "." + invocation.getMethodName(), attachments, requestMap, null,
					e.getMessage(), System.currentTimeMillis() - startTime);
			StringBuilder sb = new StringBuilder();
			sb.append("operationMessage:").append(JsonUtil.toJson(op)).append(",exception message:");
			LogUtil.requestError(sb.toString(), e);
			throw e;
		}
	}

}