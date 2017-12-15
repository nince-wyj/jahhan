package net.jahhan.init.initer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.util.internal.StringUtil;

import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.config.ArgumentConfig;
import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.frameworkx.annotation.Controller;
import com.frameworkx.annotation.DubboArgument;
import com.frameworkx.annotation.DubboMethod;
import com.frameworkx.annotation.Reference;
import com.google.inject.Injector;

import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 1600)
public class DubboIniter implements BootstrapInit {
	@Inject
	private Injector injector;

	@Override
	public void execute() {
		String[] packages = PackageUtil.packages("controller", "service");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		List<String> classNameList = new ClassScaner().parse(packages);
		for (String className : classNameList) {
			Class<?> scanClass = null;
			try {
				scanClass = classLoader.loadClass(className);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			Controller controller = (Controller) scanClass.getAnnotation(Controller.class);
			Named named = (Named) scanClass.getAnnotation(Named.class);
			if (null != controller) {
				ServiceConfig<Object> serviceConfig = new ServiceConfig<>(controller);
				Method[] methods = Controller.class.getMethods();
				for (Method method : methods) {
					String methodName = method.getName();
					if (methodName.equals("local") || methodName.equals("stub")) {
						try {
							String value = (String) method.invoke(controller);
							if (StringUtils.isBlank(value)) {
								continue;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					try {
						Method serviceMmethod = ServiceConfig.class.getMethod("set" + toUpperCaseFirstOne(methodName),
								new Class[] { method.getReturnType() });
						Object o = method.invoke(controller);
						if (void.class != o && null != o && !StringUtils.isBlank(o.toString())) {
							serviceMmethod.invoke(serviceConfig, o);
						}
					} catch (NoSuchMethodException e) {
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (void.class.equals(controller.interfaceClass()) && "".equals(controller.interfaceName())) {
					if (scanClass.getInterfaces().length > 0) {
						serviceConfig.setInterface(scanClass.getInterfaces()[0]);
					} else {
						throw new IllegalStateException("Failed to export remote service class " + scanClass.getName()
								+ ", cause: The @Controller undefined interfaceClass or interfaceName, and the service class unimplemented any interfaces.");
					}
				}
				if (scanClass.getInterfaces().length > 0) {
					serviceConfig.setMethods(getMethodConfigs(scanClass.getInterfaces()[0], true));
				}
				try {
					serviceConfig.setRef(injector.getInstance(scanClass));
				} catch (Exception e) {
					e.printStackTrace();
				}

				serviceConfig.export();
			}

			if (null != named || null != controller) {
				Object object = injector.getInstance(scanClass);
				Field[] fields = scanClass.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];

					Class<?> referenceClass = field.getType();

					Reference reference = field.getAnnotation(Reference.class);
					if (null != reference) {
						ReferenceConfig<Object> referenceConfig = new ReferenceConfig<>(reference);
						Method[] methods = Reference.class.getMethods();
						for (Method method : methods) {
							String methodName = method.getName();
							if (methodName.equals("local") || methodName.equals("stub")) {
								try {
									String value = (String) method.invoke(reference);
									if (StringUtils.isBlank(value)) {
										continue;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							try {
								Method referenceMmethod = ReferenceConfig.class.getMethod(
										"set" + toUpperCaseFirstOne(methodName),
										new Class[] { method.getReturnType() });
								Object o = method.invoke(reference);
								if (void.class != o && null != o && !StringUtils.isBlank(o.toString())) {
									referenceMmethod.invoke(referenceConfig, o);
								}
							} catch (NoSuchMethodException e) {
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						if (void.class.equals(reference.interfaceClass()) && "".equals(reference.interfaceName())
								&& referenceClass.isInterface()) {
							referenceConfig.setInterface(referenceClass);
						}
						if (referenceClass.getInterfaces().length > 0) {
							referenceConfig.setMethods(getMethodConfigs(referenceClass.getInterfaces()[0], false));
						} else if (referenceClass.isInterface()) {
							referenceConfig.setMethods(getMethodConfigs(referenceClass, false));
						}
						referenceConfig.setProxy(ConfigUtils.getProperty("dubbo.service.proxy", "javassist"));
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
						// argumentConfig.setIndex(i);
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

	public static String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
	}
}
