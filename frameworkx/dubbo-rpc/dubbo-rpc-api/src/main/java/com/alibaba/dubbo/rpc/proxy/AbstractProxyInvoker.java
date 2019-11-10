/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.proxy;

import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.ExceptionMessage;
import net.jahhan.common.extension.exception.HttpException;
import net.jahhan.common.extension.exception.HttpExceptionMessage;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.JsonUtil;

/**
 * InvokerWrapper
 * 
 * @author william.liangf
 */
@Slf4j
public abstract class AbstractProxyInvoker<T> implements Invoker<T> {

	private final T proxy;

	private final Class<T> type;

	private final URL url;

	public AbstractProxyInvoker(T proxy, Class<T> type, URL url) {
		if (proxy == null) {
			throw new IllegalArgumentException("proxy == null");
		}
		if (type == null) {
			throw new IllegalArgumentException("interface == null");
		}
		if (!type.isInstance(proxy)) {
			throw new IllegalArgumentException(proxy.getClass().getName() + " not implement interface " + type);
		}
		this.proxy = proxy;
		this.type = type;
		this.url = url;
	}

	public Class<T> getInterface() {
		return type;
	}

	public URL getUrl() {
		return url;
	}

	public boolean isAvailable() {
		return true;
	}

	public void destroy() {
	}

	public Result invoke(Invocation invocation) throws JahhanException {
		try {
			return new RpcResult(doInvoke(proxy, invocation.getMethodName(), invocation.getParameterTypes(),
					invocation.getArguments()));
		} catch (InvocationTargetException e) {
			log.debug(e.getMessage(), e);
			int httpStatus = 500;
			String code = JahhanErrorCode.UNKNOW_ERROR;
			String message = e.getTargetException().getMessage();

			Throwable targetException = e.getTargetException();
			Throwable cause = e.getCause().getCause();
			if (targetException instanceof HttpException) {// 判断异常是否是调用其他服务抛出的JahhanException异常
				HttpException exception = (HttpException) targetException;
				httpStatus = exception.getHttpStatus();
				code = exception.getExceptionMessage().getCode();
				message = exception.getExceptionMessage().getMessage();
			} else if (targetException instanceof JahhanException) {// 判断异常是否是调用其他服务抛出的JahhanException异常
				JahhanException exception = (JahhanException) targetException;
				code = exception.getExceptionMessage().getCode();
				message = exception.getExceptionMessage().getMessage();
			}

			if (e.getTargetException() instanceof InternalServerErrorException) {
				Response response = ((ServerErrorException) e.getCause()).getResponse();
				String readEntity = response.readEntity(String.class);
				ExceptionMessage serverException = JsonUtil.fromJson(readEntity.replace("\r", "").replace("\n", ""),
						ExceptionMessage.class);
				code = serverException.getCode();
				message = serverException.getMessage();
				if (serverException instanceof HttpExceptionMessage) {
					httpStatus = ((HttpExceptionMessage) serverException).getHttpStatus();
				}
			}
			return new RpcResult(new HttpException(httpStatus, code, message, cause));
		} catch (Throwable e) {
			throw new JahhanException("Failed to invoke remote proxy method " + invocation.getMethodName() + " to "
					+ getUrl() + ", cause: " + e.getMessage(), e);
		}
	}

	protected abstract Object doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments)
			throws Throwable;

	@Override
	public String toString() {
		return getInterface() + " -> " + getUrl() == null ? " " : getUrl().toString();
	}
}