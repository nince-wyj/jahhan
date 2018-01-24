package net.jahhan.remoting.http.tomcat;

import java.io.File;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.servlet.ServletManager;
import com.alibaba.dubbo.remoting.http.support.AbstractHttpServer;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.context.Node;

@Slf4j
public class TomcatHttpServer extends AbstractHttpServer {

	private final Tomcat tomcat;

	private final URL url;

	public TomcatHttpServer(URL url, final HttpHandler handler) {
		super(url, handler);
		this.url = url;
		String baseDir = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();
		tomcat = new Tomcat();
		tomcat.setBaseDir(baseDir);
		tomcat.setPort(url.getPort());
		tomcat.getConnector().setProperty("maxThreads",
				String.valueOf(url.getParameter(Constants.THREADS_KEY, Constants.DEFAULT_THREADS)));

		tomcat.getConnector().setProperty("maxConnections",
				String.valueOf(url.getParameter(Constants.ACCEPTS_KEY, -1)));

		tomcat.getConnector().setProperty("URIEncoding", "UTF-8");
		tomcat.getConnector().setProperty("connectionTimeout", "60000");

		tomcat.getConnector().setProperty("maxKeepAliveRequests", "-1");
		Context context = tomcat.addContext("", baseDir);

		AprLifecycleListener aprLifecycleListener = new AprLifecycleListener();
		aprLifecycleListener.setSSLEngine("on");
		tomcat.getServer().addLifecycleListener(aprLifecycleListener);
		context.addLifecycleListener(aprLifecycleListener);
		if (AprLifecycleListener.isAprAvailable()) {
			tomcat.getConnector().setProtocol("org.apache.coyote.http11.Http11AprProtocol");
		} else {
			tomcat.getConnector().setProtocol("org.apache.coyote.http11.Http11NioProtocol");
		}

		String[] listenerPackages = PackageUtil.packages("listener");
		List<String> listenerClassNameList = new ClassScaner().parse(listenerPackages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String className : listenerClassNameList) {
			try {
				Class<?> scanClass = classLoader.loadClass(className);
				if (scanClass.isAnnotationPresent(WebListener.class)
						&& ServletContextListener.class.isAssignableFrom(scanClass)) {
					context.addApplicationListener(className);
				}
			} catch (Exception e) {
			}
		}

		String[] filterPackages = PackageUtil.packages("filter");
		List<String> filterClassNameList = new ClassScaner().parse(filterPackages);
		for (String className : filterClassNameList) {
			try {
				Class<?> scanClass = classLoader.loadClass(className);
				if (scanClass.isAnnotationPresent(WebFilter.class) && Filter.class.isAssignableFrom(scanClass)) {
					WebFilter webFilter = scanClass.getAnnotation(WebFilter.class);
					FilterDef filterDef = new FilterDef();
					filterDef.setFilter((Filter) scanClass.newInstance());
					filterDef.setFilterName(webFilter.filterName());
					WebInitParam[] initParams = webFilter.initParams();
					for (int i = 0; i < initParams.length; i++) {
						filterDef.addInitParameter(initParams[i].name(), initParams[i].value());
					}

					context.addFilterDef(filterDef);
					FilterMap filterMap = new FilterMap();
					filterMap.setFilterName(webFilter.filterName());
					String[] urlPatterns = webFilter.urlPatterns();
					for (int i = 0; i < urlPatterns.length; i++) {
						filterMap.addURLPattern(urlPatterns[i]);
					}
					context.addFilterMap(filterMap);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String[] packages = PackageUtil.packages("servlet");
		List<String> classNameList = new ClassScaner().parse(packages);
		for (String className : classNameList) {
			try {
				Class<?> scanClass = classLoader.loadClass(className);
				if (scanClass.isAnnotationPresent(WebServlet.class) && HttpServlet.class.isAssignableFrom(scanClass)) {
					WebServlet webServlet = scanClass.getAnnotation(WebServlet.class);
					String name = webServlet.name();

					Tomcat.addServlet(context, name, (HttpServlet) scanClass.newInstance());
					String[] urlPatterns = webServlet.urlPatterns();
					for (int i = 0; i < urlPatterns.length; i++) {
						context.addServletMappingDecoded(urlPatterns[i], name);
					}
				}
			} catch (ClassNotFoundException e) {
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		Node.getInstance().addServletContext(url.getPort(), context.getServletContext());

		try {
			tomcat.start();
		} catch (LifecycleException e) {
			throw new IllegalStateException("Failed to start tomcat server at " + url.getAddress(), e);
		}
	}

	public void close() {
		super.close();

		ServletManager.getInstance().removeServletContext(url.getPort());

		try {
			tomcat.stop();
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}
}