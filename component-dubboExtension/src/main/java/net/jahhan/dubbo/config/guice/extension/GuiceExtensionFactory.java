package net.jahhan.dubbo.config.guice.extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.extension.ExtensionFactory;
import com.google.inject.Injector;

import net.jahhan.context.AppContext;

public class GuiceExtensionFactory implements ExtensionFactory {

	private static Injector injector;
	private Logger logger = LoggerFactory.getLogger(AppContext.class);

	public static Injector getInjector() {
		return injector;
	}

	public static void setInjector(Injector injector) {
		GuiceExtensionFactory.injector = injector;
	}

	public <T> T getExtension(Class<T> type, String name) {
		try {
			return injector.getInstance(type);
		} catch (Exception e) {
			logger.error("扩展点无法配置："+type.getName());
		}
		return null;
	}
}
