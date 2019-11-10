package org.springframework.guice.injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.context.ConfigurableApplicationContext;
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
	private ConfigurableApplicationContext context;

	public ConfigurableApplicationContext getContext() {
		return context;
	}

	public Injector initContext(Class<?>[] array) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(array);
		this.context = context;
		return injector;
	}

	public Injector initContext(Class<?>[] array, String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = Class.forName("org.springframework.boot.builder.SpringApplicationBuilder");
		Constructor<?> constructor = clazz.getConstructor((new Object[array.length]).getClass());
		Object obj = constructor.newInstance((Object)array);
		Method method = clazz.getMethod("run", (new String[args.length]).getClass());
		ConfigurableApplicationContext context = (ConfigurableApplicationContext) method.invoke(obj, (Object)args);
		this.context = context;
		return injector;
	}

	public Injector getInjector() {
		return injector;
	}

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

}
