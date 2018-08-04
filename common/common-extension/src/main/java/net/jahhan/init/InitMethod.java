package net.jahhan.init;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.guice.injector.InjectorHolder;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.constant.InjectType;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.common.extension.utils.PropertiesUtil;

public class InitMethod {
	public static boolean init = false;
	private static boolean isWeb = true;

	public InitMethod(boolean isWeb) {
		super();
		InitMethod.init = true;
		InitMethod.isWeb = isWeb;
	}

	private Injector injector;

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

	public void init() {
		Map<Integer, WorkHandler> workHandlerMap = new TreeMap<>();
		Set<WorkHandler> lazyWorkHandlerSet = new HashSet<>();
		try {
			String[] packages = PackageUtil.packages("init.initer");
			List<String> classNameList = new ClassScaner().parse(packages);
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			for (String className : classNameList) {
				Class<?> clazz = classLoader.loadClass(className);
				if (clazz.isAnnotationPresent(InitAnnocation.class) && BootstrapInit.class.isAssignableFrom(clazz)) {
					InitAnnocation serviceContextListener = clazz.getAnnotation(InitAnnocation.class);
					boolean isLazy = serviceContextListener.isLazy();
					boolean initOverWait = serviceContextListener.initOverWait();

					if (!isWeb && serviceContextListener.onlyWeb()) {
						continue;
					}
					final WorkHandler workHandler = (BootstrapInit) injector.getInstance(clazz);
					if (!initOverWait) {
						Thread thread = new Thread() {
							public void run() {
								try {
									workHandler.execute();
								} catch (Exception e) {
									LogUtil.error(e.getMessage(), e);
								}
							}
						};
						thread.start();
						continue;
					}
					if (isLazy) {
						lazyWorkHandlerSet.add(workHandler);
						continue;
					} else {
						if (workHandlerMap.containsKey(serviceContextListener.initSequence()))
							JahhanException.throwException(JahhanErrorCode.INIT_ERROR, "启动器包含相同启动序列！");
						workHandlerMap.put(serviceContextListener.initSequence(), workHandler);
					}
				}
			}
			Iterator<Integer> workHandlerIt = workHandlerMap.keySet().iterator();
			while (workHandlerIt.hasNext()) {
				WorkHandler workHandler = workHandlerMap.get(workHandlerIt.next());
				workHandler.execute();
			}
			Iterator<WorkHandler> lazyWorkHandlerIt = lazyWorkHandlerSet.iterator();
			ExecutorService service = Executors.newFixedThreadPool(20);
			while (lazyWorkHandlerIt.hasNext()) {
				final WorkHandler workHandler = lazyWorkHandlerIt.next();
				Thread thread = new Thread() {
					public void run() {
						try {
							workHandler.execute();
						} catch (Exception e) {
							LogUtil.error(e.getMessage(), e);
						}
					}
				};
				service.execute(thread);
			}

			service.shutdown();
			service.awaitTermination(10, TimeUnit.SECONDS);
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException inex) {
				LogUtil.error(inex.getMessage(), inex);
			}
			System.exit(-1);
		}
	}

	public static ModuleHolder orderModule() {
		Map<Integer, Class<?>> moduleMap = new TreeMap<>();
		Set<Class<?>> lazyModuleSet = new HashSet<>();
		try {
			String[] packages = PackageUtil.packages("init.module");
			List<String> classNameList = new ClassScaner().parse(packages);
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			boolean isLazy = true;
			for (String className : classNameList) {
				Class<?> clazz = classLoader.loadClass(className);
				if (clazz.isAnnotationPresent(InitAnnocation.class) && Module.class.isAssignableFrom(clazz)) {
					InitAnnocation serviceContextListener = clazz.getAnnotation(InitAnnocation.class);
					isLazy = serviceContextListener.isLazy();
					if (!isWeb && serviceContextListener.onlyWeb()) {
						continue;
					}
					if (isLazy) {
						lazyModuleSet.add(clazz);
						continue;
					} else {
						if (moduleMap.containsKey(serviceContextListener.initSequence()))
							JahhanException.throwException(JahhanErrorCode.INIT_ERROR, "module包含相同启动序列！");
						moduleMap.put(serviceContextListener.initSequence(), clazz);
					}
				}
			}
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException inex) {
				LogUtil.error(inex.getMessage(), inex);
			}
			System.exit(-1);
		}
		ModuleHolder moduleHolder = new ModuleHolder();
		moduleHolder.setLazyModuleSet(lazyModuleSet);
		moduleHolder.setModuleMap(moduleMap);
		return moduleHolder;
	}

	public Injector getInjector(String[] args) {
		ModuleHolder orderModule = orderModule();
		Map<Integer, Class<?>> moduleMap = orderModule.getModuleMap();
		Set<Class<?>> lazyModuleSet = orderModule.getLazyModuleSet();
		try {
			List<Module> moduleList = new ArrayList<>();
			Iterator<Integer> workHandlerIt = moduleMap.keySet().iterator();
			while (workHandlerIt.hasNext()) {
				Class<?> moduleClass = moduleMap.get(workHandlerIt.next());
				Module module = (Module) moduleClass.newInstance();
				moduleList.add(module);
			}
			if (BaseConfiguration.INJECT_TYPE.equals(InjectType.guice)) {
				Iterator<Class<?>> lazyWorkHandlerIt = lazyModuleSet.iterator();
				while (lazyWorkHandlerIt.hasNext()) {
					Class<?> moduleClass = lazyWorkHandlerIt.next();
					Module module = (Module) moduleClass.newInstance();
					moduleList.add(module);
				}
				injector = Guice.createInjector(moduleList.toArray(new Module[moduleList.size()]));
			} else if (BaseConfiguration.INJECT_TYPE.equals(InjectType.spring)) {
				Collection<Class<?>> values = moduleMap.values();
				String applicationClasses = PropertiesUtil.get("base", "applicationClasses");
				Class<?>[] array;
				if (StringUtils.isNoneEmpty(applicationClasses)) {
					String[] split = applicationClasses.split(",");
					array = values.toArray(new Class<?>[values.size() + split.length]);
					for (int i = 0; i < split.length; i++) {
						array[values.size() + i] = Class.forName(split[i]);
					}
				} else {
					array = values.toArray(new Class<?>[values.size()]);
				}
				AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(array);
				InjectorHolder instance = InjectorHolder.getInstance();
				instance.setContext(context);
				injector = instance.getInjector();
			} else if (BaseConfiguration.INJECT_TYPE.equals(InjectType.springboot)) {
				Collection<Class<?>> values = moduleMap.values();
				String applicationClasses = PropertiesUtil.get("base", "applicationClasses");
				Class<?>[] array;
				if (StringUtils.isNoneEmpty(applicationClasses)) {
					String[] split = applicationClasses.split(",");
					array = values.toArray(new Class<?>[values.size() + split.length]);
					for (int i = 0; i < split.length; i++) {
						array[values.size() + i] = Class.forName(split[i]);
					}
				} else {
					array = values.toArray(new Class<?>[values.size()]);
				}
				SpringApplicationBuilder builder = new SpringApplicationBuilder(array);
				ConfigurableApplicationContext context = builder.run(args);
				InjectorHolder instance = InjectorHolder.getInstance();
				instance.setContext(context);
				injector = instance.getInjector();
			}
			injector.getInstance(BaseContext.class);
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException inex) {
				LogUtil.error(inex.getMessage(), inex);
			}
			System.exit(-1);
		}
		return injector;
	}
}
