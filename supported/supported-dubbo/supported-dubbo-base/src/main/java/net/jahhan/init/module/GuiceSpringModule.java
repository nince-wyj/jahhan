package net.jahhan.init.module;

import javax.inject.Provider;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.BeanFactoryProvider;
//import org.springframework.guice.module.BeanFactoryProvider;
import org.springframework.guice.module.SpringModule;

import net.jahhan.init.InitAnnocation;
import net.jahhan.init.SpringConfiguration;

@InitAnnocation(isLazy = true)
public class GuiceSpringModule extends SpringModule {

	public GuiceSpringModule(ApplicationContext context) {
		super(context);
	}

	public GuiceSpringModule(Provider<ConfigurableListableBeanFactory> beanFactoryProvider) {
		super(beanFactoryProvider);
	}

	public GuiceSpringModule() {
		this(BeanFactoryProvider.from(SpringConfiguration.class));
	}
//	public GuiceSpringModule() {
//		this(ApplicationContextHolder.getContext());
//	}
}
