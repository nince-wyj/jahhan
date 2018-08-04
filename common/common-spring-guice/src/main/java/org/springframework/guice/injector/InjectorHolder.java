package org.springframework.guice.injector;

import org.springframework.context.ConfigurableApplicationContext;

import com.google.inject.Injector;

public class InjectorHolder {
	private InjectorHolder() {
	}

	private static InjectorHolder instance = null;

	public static InjectorHolder getInstance() {
		if (instance == null) {
			synchronized (InjectorHolder.class) { 
				if (instance == null) {
					instance = new InjectorHolder();
				}
			}
		}
		return instance;
	}
	private Injector injector;
	private ConfigurableApplicationContext context;

	public ConfigurableApplicationContext getContext() {
		return context;
	}
	public void setContext(ConfigurableApplicationContext context) {
		this.context = context;
	}
	public Injector getInjector() {
		return injector;
	}
	public void setInjector(Injector injector) {
		this.injector = injector;
	}
	
}
