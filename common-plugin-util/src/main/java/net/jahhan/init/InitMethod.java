package net.jahhan.init;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.context.BaseContext;
import net.jahhan.exception.JahhanException;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.init.WorkHandler;

public class InitMethod {
	public static boolean init = false;
	private boolean isWeb = true;

	public InitMethod(boolean isWeb) {
		super();
		this.isWeb = isWeb;
	}

	private Injector injector;

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

	public void init() {
		InitMethod.init = true;
		Map<Integer, WorkHandler> workHandlerMap = new TreeMap<>();
		Set<WorkHandler> lazyWorkHandlerSet = new HashSet<>();
		BaseContext instance = injector.getInstance(BaseContext.class);
		System.setProperty("nodeId", BaseContext.CTX.getNode().getNodeId());
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

	public Injector getInjector() {
		Map<Integer, Module> moduleMap = new TreeMap<>();
		Set<Module> lazyModuleSet = new HashSet<>();
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
					Module module = (Module) clazz.newInstance();
					if (isLazy) {
						lazyModuleSet.add(module);
						continue;
					} else {
						if (moduleMap.containsKey(serviceContextListener.initSequence()))
							JahhanException.throwException(JahhanErrorCode.INIT_ERROR, "module包含相同启动序列！");
						moduleMap.put(serviceContextListener.initSequence(), module);
					}
				}
			}
			ArrayList<Module> moduleList = new ArrayList<>();
			Iterator<Integer> workHandlerIt = moduleMap.keySet().iterator();
			while (workHandlerIt.hasNext()) {
				Module module = moduleMap.get(workHandlerIt.next());
				moduleList.add(module);
			}
			Iterator<Module> lazyWorkHandlerIt = lazyModuleSet.iterator();
			while (lazyWorkHandlerIt.hasNext()) {
				Module module = lazyWorkHandlerIt.next();
				moduleList.add(module);
			}
			injector = Guice.createInjector(moduleList.toArray(new Module[moduleList.size()]));
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
