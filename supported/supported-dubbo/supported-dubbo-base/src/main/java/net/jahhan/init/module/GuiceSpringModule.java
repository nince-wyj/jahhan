package net.jahhan.init.module;

import javax.inject.Provider;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.guice.module.SpringModule;

import net.jahhan.init.ApplicationContextHolder;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = true)
public class GuiceSpringModule extends SpringModule {

	public GuiceSpringModule(ApplicationContext context) {
		super(context);
	}

	public GuiceSpringModule(Provider<ConfigurableListableBeanFactory> beanFactoryProvider) {
		super(beanFactoryProvider);
	}

	public GuiceSpringModule() {
		this(ApplicationContextHolder.initBeanFactoryProvider());
	}
	
//	public GuiceSpringModule(Collection<Class<?>> config) {
//		this(ApplicationContextHolder.initContext(config));
//	}
}
