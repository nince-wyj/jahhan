package net.jahhan.factory;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import net.jahhan.constant.enumeration.HttpConnectionEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.factory.httpclient.AsyncHttpConnection;
import net.jahhan.factory.httpclient.HttpConnection;
import net.jahhan.factory.httpclient.MultionHttpConnection;
import net.jahhan.factory.httpclient.ThreadSafeSingtonHttpConnection;

/**
 * http请求连接获取帮组类
 */
@Singleton
public class HttpConnectionUtilFactory {

	private final Map<HttpConnectionEnum, HttpConnection> httpConnectionMap = new HashMap<HttpConnectionEnum, HttpConnection>(
			3, 1);

	public HttpConnectionUtilFactory() {
		httpConnectionMap.put(HttpConnectionEnum.MULTION, new MultionHttpConnection());
		httpConnectionMap.put(HttpConnectionEnum.THREAD_SAFE_SINGTON, new ThreadSafeSingtonHttpConnection());
		httpConnectionMap.put(HttpConnectionEnum.ASYNC, new AsyncHttpConnection());
	}

	public HttpConnection getHttpClient(HttpConnectionEnum httpClientEnum) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		if (null != invocationContext && invocationContext.isAsyncClient()) {
			return httpConnectionMap.get(HttpConnectionEnum.ASYNC);
		}
		return httpConnectionMap.get(httpClientEnum);
	}
}
