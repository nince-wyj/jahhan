package net.jahhan.listener.common;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.utils.NamedThreadFactory;
import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.context.BaseContext;
import net.jahhan.context.Node;
import net.jahhan.init.InitMethod;

@WebListener
@Slf4j(topic = "message.start.info")
public class ApplicationContextListener extends GuiceServletContextListener {
	protected static InitMethod initMethod;
	public static Injector injector;

	public void contextInitialized(ServletContextEvent sce) {
		String threadNum = PropertiesUtil.get("base", "syncServlet.thread");
		// 创建线程池
		ExecutorService executor = Executors.newFixedThreadPool(null == threadNum || threadNum.equals("")
				? Runtime.getRuntime().availableProcessors() + 1 : Integer.valueOf(threadNum),
				new NamedThreadFactory("asyncHttp", false));
		sce.getServletContext().setAttribute("executor", executor);
		if (!InitMethod.init) {
			long startTime = System.currentTimeMillis();
			initMethod = new InitMethod(true);
			super.contextInitialized(sce);
			injector.getInstance(BaseContext.class);
			appInfoInit();
			Node node = BaseContext.CTX.getNode();
			Map<String, Integer> ports = node.getPorts();
			BaseContext.CTX.addServletContext(ports.get("http"), sce.getServletContext());
			init();
			log.debug("start cost:{}ms", System.currentTimeMillis() - startTime);
		}
	}

	protected void init() {
		initMethod.init();
	}

	protected Injector getInjector() {
		injector = initMethod.getInjector();
		return injector;
	}

	private void appInfoInit() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		String name = runtimeMXBean.getName();
        Integer pid = Integer.parseInt(name.substring(0, name.indexOf('@')));
		System.setProperty("pid", pid.toString());
		Node node = BaseContext.CTX.getNode();
		node.setPid(pid);
		MBeanServer mBeanServer = null;
		ArrayList<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
		if (mBeanServers.size() > 0) {
			for (MBeanServer _mBeanServer : mBeanServers) {
				mBeanServer = _mBeanServer;
				break;
			}
		}
		Set<ObjectName> objectNames = null;
		try {
			objectNames = mBeanServer.queryNames(new ObjectName("*:type=Connector,*"), null);
		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		if (objectNames == null || objectNames.size() <= 0) {
			throw new IllegalStateException("没有发现JVM中关联的MBeanServer : " + mBeanServer.getDefaultDomain() + " 中的对象名称.");
		}
		for (ObjectName objectName : objectNames) {
			try {
				String protocol = (String) mBeanServer.getAttribute(objectName, "protocol");
				if (protocol.equals("HTTP/1.1")) {
					int port = (Integer) mBeanServer.getAttribute(objectName, "port");
					Map<String, Integer> ports = node.getPorts();
					ports.put("http",port);
					System.setProperty("port", String.valueOf(port));
					break;
				}
			} catch (AttributeNotFoundException e) {
				e.printStackTrace();
			} catch (InstanceNotFoundException e) {
				e.printStackTrace();
			} catch (MBeanException e) {
				e.printStackTrace();
			} catch (ReflectionException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) sce.getServletContext().getAttribute("executor");
		executor.shutdown();
	}

}
