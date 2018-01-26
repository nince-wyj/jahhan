package org.springframework.guice.injector;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
	private AnnotationConfigApplicationContext context;

	public AnnotationConfigApplicationContext getContext() {
		return context;
	}
	public void setContext(AnnotationConfigApplicationContext context) {
		this.context = context;
	}
	public Injector getInjector() {
		return injector;
	}
	public void setInjector(Injector injector) {
		this.injector = injector;
	}
	
}
