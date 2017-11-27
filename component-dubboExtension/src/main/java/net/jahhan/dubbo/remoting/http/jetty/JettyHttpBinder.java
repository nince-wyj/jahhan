package net.jahhan.dubbo.remoting.http.jetty;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;

public class JettyHttpBinder implements HttpBinder {

	public HttpServer bind(URL url, HttpHandler handler) {
		return new JettyHttpServer(url, handler);
	}

}