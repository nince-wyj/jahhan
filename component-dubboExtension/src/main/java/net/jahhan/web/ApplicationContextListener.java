package net.jahhan.web;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import net.jahhan.constant.enumeration.ThreadPoolEnum;
import net.jahhan.factory.ThreadPoolFactory;
import net.jahhan.init.InitMethod;

@WebListener
public class ApplicationContextListener extends GuiceServletContextListener {

	protected static InitMethod initMethod;
	public static Injector injector;

	public void contextInitialized(ServletContextEvent sce) {
		initMethod = new InitMethod(true);
		super.contextInitialized(sce);
		init();
		ExecutorService executeService = injector.getInstance(ThreadPoolFactory.class)
				.getExecuteService(ThreadPoolEnum.FIXED);
		sce.getServletContext().setAttribute("executor", executeService);
	}

	protected void init() {
		initMethod.init();
	}

	@Override
	protected Injector getInjector() {
		injector = initMethod.getInjector();
		return injector;
	}

	public void contextDestroyed(ServletContextEvent sce) {
		ExecutorService executeService = (ExecutorService) sce.getServletContext().getAttribute("executor");
		executeService.shutdown();
	}
}
