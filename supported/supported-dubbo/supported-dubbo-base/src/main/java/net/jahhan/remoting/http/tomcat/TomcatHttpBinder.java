package net.jahhan.remoting.http.tomcat;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;

public class TomcatHttpBinder implements HttpBinder {

	public HttpServer bind(URL url, HttpHandler handler) {
		return new TomcatHttpServer(url, handler);
	}

}