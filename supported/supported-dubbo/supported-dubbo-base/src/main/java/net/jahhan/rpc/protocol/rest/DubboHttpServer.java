package net.jahhan.rpc.protocol.rest;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.remoting.http.servlet.DispatcherServlet;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.protocol.rest.BaseRestServer;

import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.context.Node;
import net.jahhan.exception.JahhanException;
import net.jahhan.servlet.ServiceServlet;

public class DubboHttpServer extends BaseRestServer {

	private final HttpServletDispatcher dispatcher = new HttpServletDispatcher();
	private final ResteasyDeployment deployment = new ResteasyDeployment();
	private HttpBinder httpBinder;
	private HttpServer httpServer;
	// private boolean isExternalServer;

	public DubboHttpServer(HttpBinder httpBinder) {
		this.httpBinder = httpBinder;
	}

	public void start(URL url) {
		getDeployment().getMediaTypeMappings().put("json", "application/json");
		getDeployment().getMediaTypeMappings().put("xml", "text/xml");
		String[] packages = PackageUtil.packages("rest.filter");
		List<String> classNameList = new ClassScaner().parse(packages);
		for (String className : classNameList) {
			getDeployment().getProviderClasses().add(className);
		}
		loadProviders(url.getParameter(Constants.EXTENSION_KEY, ""));

		doStart(url);
	}

	protected void doStart(URL url) {
		// TODO jetty will by default enable keepAlive so the xml config has no
		// effect now
		RestHandler handler = new RestHandler();
		httpServer = httpBinder.bind(url, handler);
		ServiceServlet.addHttpHandler(url.getParameter(Constants.BIND_PORT_KEY, 8080), handler);
		ServletContext servletContext = Node.getInstance().getServletContext(url.getPort());

		if (servletContext == null) {
			throw new JahhanException("No servlet context found. If you are using server='servlet', "
					+ "make sure that you've configured  in web.xml");
		}

		servletContext.setAttribute(ResteasyDeployment.class.getName(), deployment);

		try {
			dispatcher.init(new SimpleServletConfig(servletContext));
		} catch (ServletException e) {
			throw new JahhanException(e);
		}
	}

	public void stop() {
		httpServer.close();
	}

	protected ResteasyDeployment getDeployment() {
		return deployment;
	}

	private class RestHandler implements HttpHandler {

		public void handle(HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {
			RpcContext.getContext().setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
			dispatcher.service(request, response);
		}
	}

	private static class SimpleServletConfig implements ServletConfig {

		private final ServletContext servletContext;

		public SimpleServletConfig(ServletContext servletContext) {
			this.servletContext = servletContext;
		}

		public String getServletName() {
			return "DispatcherServlet";
		}

		public ServletContext getServletContext() {
			return servletContext;
		}

		public String getInitParameter(String s) {
			return null;
		}

		public Enumeration getInitParameterNames() {
			return new Enumeration() {
				public boolean hasMoreElements() {
					return false;
				}

				public Object nextElement() {
					return null;
				}
			};
		}
	}
}
