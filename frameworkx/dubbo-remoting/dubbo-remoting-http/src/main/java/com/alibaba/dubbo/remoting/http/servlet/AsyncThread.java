package com.alibaba.dubbo.remoting.http.servlet;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.dubbo.remoting.http.HttpHandler;

import lombok.Setter;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.ThreadVariableContext;

/**
 * 接口异步线程方法
 * 
 * @author nince
 */
public class AsyncThread implements Runnable {
	@Setter
	private HttpServletRequest request;
	@Setter
	private HttpServletResponse response;
	@Setter
	private HttpHandler handler;
	@Setter
	private AsyncContext asyncCtx;

	@Override
	public void run() {
		ThreadVariableContext variableContext = new ThreadVariableContext();
		BaseContext.CTX.getThreadLocalUtil().openThreadLocal(variableContext);
		try {
			if (handler == null) {// service not found.
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Service not found.");
			} else {
				handler.handle(request, response);
			}
		} catch (IOException | ServletException e) {
			e.printStackTrace();
		} finally {
			asyncCtx.complete();
		}
	}
}
