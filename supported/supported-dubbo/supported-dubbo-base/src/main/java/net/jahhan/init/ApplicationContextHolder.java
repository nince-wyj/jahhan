package net.jahhan.init;

import java.util.Collection;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.BeanFactoryProvider;

import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;

public class ApplicationContextHolder {
	private static BeanFactoryProvider provider;

	public static BeanFactoryProvider initBeanFactoryProvider() {
		if (null == provider) {
			provider = BeanFactoryProvider.from(SpringConfiguration.class);
		}
		return provider;
	}

	public static BeanFactoryProvider initBeanFactoryProvider(Collection<Class<?>> config) {
		if (null == provider) {
			Class<?>[] array = config.toArray(new Class<?>[config.size() + 1]);
			array[config.size()] = SpringConfiguration.class;
			provider = BeanFactoryProvider.from(array);
		}
		return provider;
	}

	public static ApplicationContext getContext() {
		if (null != context) {
			return context;
		}
		Assert.notNull(provider, JahhanErrorCode.UNKNOW_ERROR);
		return provider.getApplicationContext();
	}

	private static AnnotationConfigApplicationContext context;

	public static ApplicationContext initContext(Collection<Class<?>> config) {
		if (null == context) {
			Class<?>[] array = config.toArray(new Class<?>[config.size() + 1]);
			array[config.size()] = SpringConfiguration.class;
			context = new AnnotationConfigApplicationContext(array);
			context.removeBeanDefinition("Logger");
		}
		return context;
	}
}
