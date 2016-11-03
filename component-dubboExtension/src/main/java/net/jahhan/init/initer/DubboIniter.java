package net.jahhan.init.initer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.config.ArgumentConfig;
import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.google.inject.Injector;

import net.jahhan.annotation.Job;
import net.jahhan.dubbo.annotation.DubboArgument;
import net.jahhan.dubbo.annotation.DubboInterface;
import net.jahhan.dubbo.annotation.DubboMethod;
import net.jahhan.dubbo.cache.HostCache;
import net.jahhan.dubbo.config.guice.extension.GuiceExtensionFactory;
import net.jahhan.dubbo.enumeration.ClusterTypeEnum;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.utils.ClassScaner;
import net.jahhan.web.action.annotation.ActionService;

@InitAnnocation(isLazy = false, initSequence = 1600)
public class DubboIniter implements BootstrapInit {
	@Inject
	private Injector injector;

	@Override
	public void execute() {
		GuiceExtensionFactory.setInjector(injector);
		String scanPath = ConfigUtils.getProperty("dubbo.annotation.package");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		List<String> classNameList = new ClassScaner().parse(scanPath.split(","));
		for (String className : classNameList) {
			Class<?> scanClass = null;
			try {
				scanClass = classLoader.loadClass(className);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			Service service = (Service) scanClass.getAnnotation(Service.class);
			Named named = (Named) scanClass.getAnnotation(Named.class);
			ActionService actionService = (ActionService) scanClass.getAnnotation(ActionService.class);
			Job job = (Job) scanClass.getAnnotation(Job.class);
			if (null != service) {
				ServiceConfig<Object> serviceConfig = new ServiceConfig<>(service);
				if (void.class.equals(service.interfaceClass()) && "".equals(service.interfaceName())) {
					if (scanClass.getInterfaces().length > 0) {
						serviceConfig.setInterface(scanClass.getInterfaces()[0]);
					} else {
						throw new IllegalStateException("Failed to export remote service class " + scanClass.getName()
								+ ", cause: The @Service undefined interfaceClass or interfaceName, and the service class unimplemented any interfaces.");
					}
				}
				if (scanClass.getInterfaces().length > 0) {
					serviceConfig.setMethods(getMethodConfigs(scanClass.getInterfaces()[0], true));
				}
				try {
					serviceConfig.setRef(injector.getInstance(scanClass));
				} catch (Exception e) {
				}

				serviceConfig.export();
				Class<?>[] interfaces = scanClass.getInterfaces();
				if (interfaces.length > 0) {
					URL url = serviceConfig.getExportedUrls().get(0);
					HostCache.getInstance().setPort(url.getPort());
					DubboInterface dubboInterface = interfaces[0].getAnnotation(DubboInterface.class);
					if (null != dubboInterface && dubboInterface.clusterType().equals(ClusterTypeEnum.DIRECT)) {
						HostCache.getInstance().setHost(interfaces[0].getName(), url.getHost() + ":" + url.getPort());
					}
				}
			}

			if (null != named || null != actionService || null != job) {
				Object object = injector.getInstance(scanClass);
				Field[] fields = scanClass.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];

					Class<?> referenceClass = field.getType();

					Reference reference = field.getAnnotation(Reference.class);
					if (null != reference) {
						ReferenceConfig<Object> referenceConfig = new ReferenceConfig<>(reference);
						if (void.class.equals(reference.interfaceClass()) && "".equals(reference.interfaceName())
								&& referenceClass.isInterface()) {
							referenceConfig.setInterface(referenceClass);
						}
						if (referenceClass.getInterfaces().length > 0) {
							referenceConfig.setMethods(getMethodConfigs(referenceClass.getInterfaces()[0], false));
						} else if (referenceClass.isInterface()) {
							referenceConfig.setMethods(getMethodConfigs(referenceClass, false));
						}
						try {
							field.setAccessible(true);
							field.set(object, referenceConfig.get());
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private List<MethodConfig> getMethodConfigs(Class<?> configClass, boolean isServer) {
		Method[] methods = configClass.getMethods();
		List<MethodConfig> methodConfigs = new ArrayList<>();
		for (Method method : methods) {
			DubboMethod dubboMethod = method.getAnnotation(DubboMethod.class);
			MethodConfig methodConfig = new MethodConfig();
			methodConfig.setName(method.getName());
			if (null != dubboMethod) {
				if (dubboMethod.actives() > -1) {
					methodConfig.setActives(dubboMethod.actives());
				}
				methodConfig.setAsync(dubboMethod.async());
				if (!dubboMethod.cache().equals("")) {
					methodConfig.setCache(dubboMethod.cache());
				}
				methodConfig.setDeprecated(dubboMethod.deprecated());
				if (dubboMethod.executes() > -1) {
					methodConfig.setExecutes(dubboMethod.executes());
				}
				if (!dubboMethod.loadbalance().equals("")) {
					methodConfig.setLoadbalance(dubboMethod.loadbalance());
				}
				if (!dubboMethod.merger().equals("")) {
					methodConfig.setMerger(dubboMethod.merger());
				}
				if (!dubboMethod.mock().equals("")) {
					methodConfig.setMock(dubboMethod.mock());
				}
				if (!dubboMethod.oninvokeintance().equals("") && !dubboMethod.oninvoke().equals("")) {
					try {
						Class<?> clazz = Class.forName(dubboMethod.oninvokeintance());
						methodConfig.setOninvoke(clazz.newInstance());
						methodConfig.setOninvokeMethod(dubboMethod.oninvoke());
					} catch (Exception e) {
					}

				}
				if (!dubboMethod.onreturnintance().equals("") && !dubboMethod.oninvoke().equals("")) {
					try {
						Class<?> clazz = Class.forName(dubboMethod.oninvokeintance());
						methodConfig.setOninvoke(clazz.newInstance());
						methodConfig.setOninvokeMethod(dubboMethod.oninvoke());
					} catch (Exception e) {
					}

				}
				if (!dubboMethod.onthrowintance().equals("") && !dubboMethod.onthrow().equals("")) {
					try {
						Class<?> clazz = Class.forName(dubboMethod.onthrowintance());
						methodConfig.setOninvoke(clazz.newInstance());
						methodConfig.setOninvokeMethod(dubboMethod.onthrow());
					} catch (Exception e) {
					}

				}
				if (dubboMethod.retries() > -1) {
					methodConfig.setRetries(dubboMethod.retries());
				}
				methodConfig.setReturn(dubboMethod.isReturn());
				methodConfig.setSent(dubboMethod.sent());
				methodConfig.setSticky(dubboMethod.sticky());
				if (dubboMethod.timeout() > -1) {
					methodConfig.setTimeout(dubboMethod.timeout());
				}
				if (!dubboMethod.validation().equals("")) {
					methodConfig.setValidation(dubboMethod.validation());
				}
				if (isServer) {
					List<ArgumentConfig> arguments = new ArrayList<>();
					Parameter[] parameters = method.getParameters();
					int i = 0;
					for (Parameter parameter : parameters) {
						DubboArgument annotation = parameter.getAnnotation(DubboArgument.class);
						boolean callBack = false;
						if (null != annotation) {
							callBack = annotation.callback();
						}
						ArgumentConfig argumentConfig = new ArgumentConfig();
						argumentConfig.setCallback(callBack);
//						argumentConfig.setIndex(i);
						argumentConfig.setType(parameter.getName());
						arguments.add(argumentConfig);
						i++;
					}
					methodConfig.setArguments(arguments);
				}
			}
			methodConfigs.add(methodConfig);
		}
		return methodConfigs;
	}
}
