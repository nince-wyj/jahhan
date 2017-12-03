package net.jahhan.init.module;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.compiler.support.JavassistCompiler;
import com.frameworkx.annotation.Activate;
import com.frameworkx.annotation.Adaptive;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.api.Wrapper;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.annotation.SPI;
import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.ExtensionUtil;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.init.InitAnnocation;

@Slf4j
@InitAnnocation(isLazy = false, initSequence = 4500)
public class ExtensionInitModule extends AbstractModule {

	private Map<Class<?>, Set<Class<?>>> cachedWrapperClasses = new HashMap<>();
	private com.alibaba.dubbo.common.compiler.Compiler compiler = new JavassistCompiler();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void configure() {
		String[] packages = PackageUtil.packages("extension");
		List<String> classNameList = new ClassScaner().parse(packages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Properties properties = PropertiesUtil.getProperties("extensionInit");
		Map<Class, List<Class>> unSetMap = new HashMap<>();
		Map<Class, String> spiDefaultExtensionMap = new HashMap<>();
		Set<Class> adaptiveClassSet = new HashSet<>();
		Set<Class> adaptivedClassSet = new HashSet<>();
		List<Class> settedList = new ArrayList<>();

		// wapper
		for (String className : classNameList) {
			try {
				Class<?> clazz = classLoader.loadClass(className);
				Class interfaceClass = getSuperInterfaceByAnnotation(clazz, SPI.class);
				if (null == interfaceClass) {
					continue;
				}
				if (Wrapper.class.isAssignableFrom(clazz)) {
					Set<Class<?>> wrappers = cachedWrapperClasses.get(interfaceClass);
					if (wrappers == null) {
						wrappers = new HashSet<Class<?>>();
						cachedWrapperClasses.put(interfaceClass, wrappers);
					}
					wrappers.add(clazz);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		Map<Class<?>, Map<Class<?>, String>> extensionCacheClassNameMap = ExtensionUtil.getExtensionCacheClassNameMap();
		// 先加载Adaptive适配类
		for (String className : classNameList) {
			try {
				Class<?> clazz = classLoader.loadClass(className);
				Class interfaceClass = getSuperInterfaceByAnnotation(clazz, SPI.class);
				if (null == interfaceClass) {
					continue;
				}
				SPI interfaceSPI = (SPI) interfaceClass.getAnnotation(SPI.class);
				if (clazz.isAnnotationPresent(Extension.class)) {
					Extension extension = clazz.getAnnotation(Extension.class);
					String extensionValue = extension.value();
					// 扩展类缓存
					Map<Class<?>, String> map = extensionCacheClassNameMap.get(interfaceClass);
					if (null == map) {
						map = new ConcurrentHashMap<>();
						extensionCacheClassNameMap.put(interfaceClass, map);
					}
					map.put(clazz, extensionValue);

					// 默认配置
					String defaultPlugin = properties.getProperty(interfaceClass.getName());
					if (null == defaultPlugin) {
						defaultPlugin = interfaceSPI.value();
					}
					if (extensionValue.equals(defaultPlugin)) {
						spiDefaultExtensionMap.put(interfaceClass, defaultPlugin);
					}

					Set<Class<?>> set = cachedWrapperClasses.get(interfaceClass);
					if (null != set && set.contains(clazz)) {
						continue;
					}

					Method[] interfaceMethods = interfaceClass.getMethods();
					boolean hasAdaptive = false;
					for (Method m : interfaceMethods) {
						if (m.isAnnotationPresent(Adaptive.class)) {
							hasAdaptive = true;
							break;
						}
					}
					if (hasAdaptive) {
						adaptiveClassSet.add(interfaceClass);
					}
				}
				if (clazz.isAnnotationPresent(Adaptive.class)) {
					if (log.isDebugEnabled()) {
						log.debug("bind " + interfaceClass.getName() + " named Adaptive to " + clazz.getName());
					}
					bind(interfaceClass).annotatedWith(Adaptive.class).to(clazz).in(Scopes.SINGLETON);
					adaptivedClassSet.add(interfaceClass);
				}
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}

		}

		for (Class adaptiveClass : adaptiveClassSet) {
			if (!adaptivedClassSet.contains(adaptiveClass)) {
				String cachedDefaultName = spiDefaultExtensionMap.get(adaptiveClass);
				String createAdaptiveExtensionClassCode = createAdaptiveExtensionClassCode(adaptiveClass,
						cachedDefaultName);

				Class<?> compile = compiler.compile(createAdaptiveExtensionClassCode, classLoader);
				if (log.isDebugEnabled()) {
					log.debug("bind " + adaptiveClass.getName() + " named Adaptive to " + compile.getName());
				}
				bind(adaptiveClass).annotatedWith(Adaptive.class).to(compile).in(Scopes.SINGLETON);
			}
		}

		Map<Class<?>, Map<String, Activate>> cachedActivatesMap = ExtensionExtendUtil.getCachedActivates();
		for (String className : classNameList) {
			try {
				Class<?> clazz = classLoader.loadClass(className);
				Class interfaceClass = getSuperInterfaceByAnnotation(clazz, SPI.class);
				if (null == interfaceClass) {
					continue;
				}

				if (clazz.isAnnotationPresent(Extension.class)) {
					Extension extension = clazz.getAnnotation(Extension.class);
					String extensionValue = extension.value();

					Set<Class<?>> set = cachedWrapperClasses.get(interfaceClass);
					if (null != set && set.contains(clazz)) {
						continue;
					}

					// Activate注解缓存
					Map<String, Activate> cachedActivates = cachedActivatesMap.get(interfaceClass);
					if (null == cachedActivates) {
						cachedActivates = new ConcurrentHashMap<>();
						cachedActivatesMap.put(interfaceClass, cachedActivates);
					}
					Activate activate = clazz.getAnnotation(Activate.class);
					if (activate != null) {
						cachedActivates.put(extensionValue, activate);
					}

					// 默认配置
					String defaultPlugin = properties.getProperty(interfaceClass.getName());

					Method[] interfaceMethods = interfaceClass.getMethods();
					boolean hasAdaptive = false;
					for (Method m : interfaceMethods) {
						if (m.isAnnotationPresent(Adaptive.class)) {
							hasAdaptive = true;
							break;
						}
					}
					if (hasAdaptive) {
						adaptiveClassSet.add(interfaceClass);
						List<Class> list = unSetMap.get(interfaceClass);
						if (null == list) {
							list = new ArrayList<>();
							list.add(clazz);
							unSetMap.put(interfaceClass, list);
						} else {// !null == list
							list.add(clazz);
						}

						Class<?> createExtension = createExtension(interfaceClass, clazz);
						if (log.isTraceEnabled()) {
							log.debug("bind " + interfaceClass.getName() + " named " + extensionValue + " to "
									+ createExtension.getName());
						}
						bind(interfaceClass).annotatedWith(Names.named(extensionValue)).to(createExtension)
								.in(Scopes.SINGLETON);
					} else {// !hasAdaptive
						if (extensionValue.equals(defaultPlugin)) {
							Class<?> createExtension = createExtension(interfaceClass, clazz);
							if (null != set && set.size() > 0) {
								if (log.isDebugEnabled()) {
									log.debug("bind " + interfaceClass.getName() + " to " + createExtension.getName());
									log.debug("bind " + interfaceClass.getName() + " named " + extensionValue + " to "
											+ createExtension.getName());
								}
								bind(interfaceClass).toInstance(createExtension);
								bind(interfaceClass).annotatedWith(Names.named(extensionValue))
										.toInstance(createExtension);
							} else {
								if (log.isDebugEnabled()) {
									log.debug("bind " + interfaceClass.getName() + " to " + createExtension.getName());
									log.debug("bind " + interfaceClass.getName() + " named " + extensionValue + " to "
											+ createExtension.getName());
								}
								bind(interfaceClass).to(createExtension).in(Scopes.SINGLETON);
								bind(interfaceClass).annotatedWith(Names.named(extensionValue)).to(createExtension)
										.in(Scopes.SINGLETON);
							}
							settedList.add(interfaceClass);
						} else {// !extension.value().equals(defaultPlugin)
							Class<?> createExtension = createExtension(interfaceClass, clazz);
							if (null != set && set.size() > 0) {
								if (log.isDebugEnabled()) {
									log.debug("bind " + interfaceClass.getName() + " to " + createExtension.getName());
								}
								bind(interfaceClass).to(createExtension).in(Scopes.SINGLETON);
							} else {
								if (log.isDebugEnabled()) {
									log.debug("bind " + interfaceClass.getName() + " named " + extensionValue + " to "
											+ createExtension.getName());
								}
								bind(interfaceClass).annotatedWith(Names.named(extensionValue)).to(createExtension)
										.in(Scopes.SINGLETON);
							}
							List<Class> list = unSetMap.get(interfaceClass);
							if (null == list) {
								list = new ArrayList<>();
								list.add(clazz);
								unSetMap.put(interfaceClass, list);
							} else {// !null == list
								list.add(clazz);
							}
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

		}
		Iterator<Class> iterator = unSetMap.keySet().iterator();
		while (iterator.hasNext()) {
			Class next = iterator.next();
			if (settedList.contains(next)) {
				continue;
			}
			Class<?> createExtension = createExtension(next, unSetMap.get(next).get(0));
			if (log.isDebugEnabled()) {
				log.debug("bind " + next.getName() + " to" + createExtension.getName());
			}
			bind(next).to(createExtension).in(Scopes.SINGLETON);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Class<?> createExtension(Class<T> intefaceClass, Class<? extends T> defaultClass) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Map<Class<?>, Map<Class<?>, String>> extensionCacheClassNameMap = ExtensionUtil.getExtensionCacheClassNameMap();
		try {
			Set<Class<?>> set = cachedWrapperClasses.get(intefaceClass);
			if (null != set && set.size() > 0) {
				Map<Class<?>, String> map = extensionCacheClassNameMap.get(intefaceClass);
				String nextExtName = map.get(defaultClass);
				Class<? extends T> compile = defaultClass;
				for (Class<?> wrapperClass : set) {
					String extName = map.get(wrapperClass);
					extName = extName + "$wrapper$" + nextExtName;
					String createWrapperExtensionClassCode = createWrapperExtensionClassCode(intefaceClass, extName,
							nextExtName);
					compile = (Class<? extends T>) compiler.compile(createWrapperExtensionClassCode, classLoader);
					if (log.isDebugEnabled()) {
						log.debug("bind " + intefaceClass + " named $" + extName + " to " + wrapperClass.getName());
					}
					bind(intefaceClass).annotatedWith(Names.named("$" + extName)).to((Class<? extends T>) wrapperClass)
							.in(Scopes.SINGLETON);
					nextExtName = extName;
					if (log.isDebugEnabled()) {
						log.debug(
								"bind " + intefaceClass + " named wrapper$" + nextExtName + " to " + compile.getName());
					}
					bind(intefaceClass).annotatedWith(Names.named("wrapper$" + nextExtName)).to(compile)
							.in(Scopes.SINGLETON);

				}
				if (null != compile) {
					bind(intefaceClass).annotatedWith(Names.named("$" + map.get(defaultClass))).to(defaultClass)
							.in(Scopes.SINGLETON);
					return compile;
				}

			} else {
				return defaultClass;
			}
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return defaultClass;
	}

	private String createAdaptiveExtensionClassCode(Class<?> type, String cachedDefaultName) {
		StringBuilder codeBuidler = new StringBuilder();
		Method[] methods = type.getMethods();
		codeBuidler.append("// dubbo extension code:");
		codeBuidler.append(type.getName());
		codeBuidler.append("\npackage " + type.getPackage().getName() + ";");
		codeBuidler.append("\nimport " + ExtensionUtil.class.getName() + ";");
		codeBuidler.append("\npublic class " + type.getSimpleName() + "$Adpative" + " implements "
				+ type.getCanonicalName() + " {");

		for (Method method : methods) {
			Class<?> rt = method.getReturnType();
			Class<?>[] pts = method.getParameterTypes();
			Class<?>[] ets = method.getExceptionTypes();

			Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
			StringBuilder code = new StringBuilder(512);
			if (adaptiveAnnotation == null) {
				code.append("throw new UnsupportedOperationException(\"method ").append(method.toString())
						.append(" of interface ").append(type.getName()).append(" is not adaptive method!\");");
			} else {
				int urlTypeIndex = -1;
				for (int i = 0; i < pts.length; ++i) {
					if (pts[i].equals(URL.class)) {
						urlTypeIndex = i;
						break;
					}
				}
				// 有类型为URL的参数
				if (urlTypeIndex != -1) {
					// Null Point check
					String s = String.format(
							"\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");", urlTypeIndex);
					code.append(s);

					s = String.format("\n%s url = arg%d;", URL.class.getName(), urlTypeIndex);
					code.append(s);
				}
				// 参数没有URL类型
				else {
					String attribMethod = null;

					// 找到参数的URL属性
					LBL_PTS: for (int i = 0; i < pts.length; ++i) {
						Method[] ms = pts[i].getMethods();
						for (Method m : ms) {
							String name = m.getName();
							if ((name.startsWith("get") || name.length() > 3) && Modifier.isPublic(m.getModifiers())
									&& !Modifier.isStatic(m.getModifiers()) && m.getParameterTypes().length == 0
									&& m.getReturnType() == URL.class) {
								urlTypeIndex = i;
								attribMethod = name;
								break LBL_PTS;
							}
						}
					}
					if (attribMethod == null) {
						throw new IllegalStateException("fail to create adative class for interface " + type.getName()
								+ ": not found url parameter or url attribute in parameters of method "
								+ method.getName());
					}

					// Null point check
					String s = String.format(
							"\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");",
							urlTypeIndex, pts[urlTypeIndex].getName());
					code.append(s);
					s = String.format(
							"\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");",
							urlTypeIndex, attribMethod, pts[urlTypeIndex].getName(), attribMethod);
					code.append(s);

					s = String.format("%s url = arg%d.%s();", URL.class.getName(), urlTypeIndex, attribMethod);
					code.append(s);
				}

				String[] value = adaptiveAnnotation.value();
				// 没有设置Key，则使用“扩展点接口名的点分隔 作为Key
				if (value.length == 0) {
					char[] charArray = type.getSimpleName().toCharArray();
					StringBuilder sb = new StringBuilder(128);
					for (int i = 0; i < charArray.length; i++) {
						if (Character.isUpperCase(charArray[i])) {
							if (i != 0) {
								sb.append(".");
							}
							sb.append(Character.toLowerCase(charArray[i]));
						} else {
							sb.append(charArray[i]);
						}
					}
					value = new String[] { sb.toString() };
				}

				boolean hasInvocation = false;
				for (int i = 0; i < pts.length; ++i) {
					if (pts[i].getName().equals("com.alibaba.dubbo.rpc.Invocation")) {
						// Null Point check
						String s = String.format(
								"\nif (arg%d == null) throw new IllegalArgumentException(\"invocation == null\");", i);
						code.append(s);
						s = String.format("\nString methodName = arg%d.getMethodName();", i);
						code.append(s);
						hasInvocation = true;
						break;
					}
				}

				String defaultExtName = cachedDefaultName;
				String getNameCode = null;
				for (int i = value.length - 1; i >= 0; --i) {
					if (i == value.length - 1) {
						if (null != defaultExtName) {
							if (!"protocol".equals(value[i]))
								if (hasInvocation)
									getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")",
											value[i], defaultExtName);
								else
									getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i],
											defaultExtName);
							else
								getNameCode = String.format(
										"( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
						} else {
							if (!"protocol".equals(value[i]))
								if (hasInvocation)
									getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")",
											value[i], defaultExtName);
								else
									getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
							else
								getNameCode = "url.getProtocol()";
						}
					} else {
						if (!"protocol".equals(value[i]))
							if (hasInvocation)
								getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")",
										value[i], defaultExtName);
							else
								getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
						else
							getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()",
									getNameCode);
					}
				}
				code.append("\nString extName = ").append(getNameCode).append(";");
				// check extName == null?
				String s = String.format(
						"\nif(extName == null) "
								+ "throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
						type.getName(), Arrays.toString(value));
				code.append(s);

				s = String.format("\n%s extension = (%<s)%s.getExtension(%s.class,extName);", type.getName(),
						ExtensionUtil.class.getSimpleName(), type.getName());
				code.append(s);

				// return statement
				if (!rt.equals(void.class)) {
					code.append("\nreturn ");
				}

				s = String.format("extension.%s(", method.getName());
				code.append(s);
				for (int i = 0; i < pts.length; i++) {
					if (i != 0)
						code.append(", ");
					code.append("arg").append(i);
				}
				code.append(");");
			}

			codeBuidler.append("\npublic " + rt.getCanonicalName() + " " + method.getName() + "(");
			for (int i = 0; i < pts.length; i++) {
				if (i > 0) {
					codeBuidler.append(", ");
				}
				codeBuidler.append(pts[i].getCanonicalName());
				codeBuidler.append(" ");
				codeBuidler.append("arg" + i);
			}
			codeBuidler.append(")");
			if (ets.length > 0) {
				codeBuidler.append(" throws ");
				for (int i = 0; i < ets.length; i++) {
					if (i > 0) {
						codeBuidler.append(", ");
					}
					codeBuidler.append(ets[i].getCanonicalName());
				}
			}
			codeBuidler.append(" {");
			codeBuidler.append(code.toString());
			codeBuidler.append("\n}");
		}
		codeBuidler.append("\n}");
		log.debug(codeBuidler.toString());
		return codeBuidler.toString();
	}

	private String createWrapperExtensionClassCode(Class<?> type, String extName, String nextExtName) {
		StringBuilder codeBuidler = new StringBuilder();
		Method[] methods = type.getMethods();
		codeBuidler.append("// dubbo extension code:");
		codeBuidler.append(type.getName());
		codeBuidler.append("\npackage " + type.getPackage().getName() + ";");
		codeBuidler.append("\nimport " + ExtensionUtil.class.getName() + ";");
		codeBuidler.append("\nimport javax.inject.Singleton;");
		codeBuidler.append("\n");
		codeBuidler.append("\n@Singleton");
		codeBuidler.append("\npublic class " + type.getSimpleName() + "$Wrapper$" + extName + " implements "
				+ type.getCanonicalName() + " {");
		codeBuidler.append("\nprivate " + type.getName() + " extension = null;");
		codeBuidler.append("\nprivate final String extName = \"" + extName + "\";");
		codeBuidler.append("\npublic " + type.getSimpleName() + "$Wrapper$" + extName + "(){");
		StringBuilder code = new StringBuilder(512);
		code.append("\nString extensionName=extName;");
		code.append(String.format("\n extension = (%s)%s.getExtensionDirect(%s.class,extName);", type.getName(),
				ExtensionUtil.class.getSimpleName(), type.getName()));
		code.append("\nif(extName.contains(\"wrapper\\$\")){");

		code.append("\n" + Wrapper.class.getName() + " wrapper = (" + Wrapper.class.getName() + ") extension;");
		code.append("\nextensionName = extName.substring(extName.indexOf(\"wrapper\\$\")+8);");
		code.append("\nwrapper.setExtName(extensionName);");
		code.append(String.format("\nwrapper.setIntefaceClass(%s.class);", type.getName()));
		code.append("\nwrapper.initExtension();");
		code.append("\n}");
		code.append("\n}");

		codeBuidler.append(code);
		codeBuidler.append("\n}");

		for (Method method : methods) {
			Class<?> rt = method.getReturnType();
			Class<?>[] pts = method.getParameterTypes();
			Class<?>[] ets = method.getExceptionTypes();

			StringBuilder code2 = new StringBuilder(512);
			if (!rt.equals(void.class)) {
				code2.append("\nreturn ");
			}

			String s = String.format("extension.%s(", method.getName());
			code2.append(s);
			for (int i = 0; i < pts.length; i++) {
				if (i != 0)
					code2.append(", ");
				code2.append("arg").append(i);
			}
			code2.append(");");
			codeBuidler.append("\npublic " + rt.getCanonicalName() + " " + method.getName() + "(");
			for (int i = 0; i < pts.length; i++) {
				if (i > 0) {
					codeBuidler.append(", ");
				}
				codeBuidler.append(pts[i].getCanonicalName());
				codeBuidler.append(" ");
				codeBuidler.append("arg" + i);
			}
			codeBuidler.append(")");
			if (ets.length > 0) {
				codeBuidler.append(" throws ");
				for (int i = 0; i < ets.length; i++) {
					if (i > 0) {
						codeBuidler.append(", ");
					}
					codeBuidler.append(ets[0].getName());
				}
			}
			codeBuidler.append(" {");
			codeBuidler.append(code2.toString());
			codeBuidler.append("\n}");
		}
		codeBuidler.append("\n}");
		log.debug(codeBuidler.toString());
		return codeBuidler.toString();
	}

	private static Class<?> getSuperInterfaceByAnnotation(Class<?> type, Class<? extends Annotation> annotation) {
		List<Class<?>> superClasses = new ArrayList<>();
		superClasses.add(type);
		Class<?> superclass = type;
		while ((superclass = superclass.getSuperclass()) != null) {
			superClasses.add(superclass);
		}
		for (Class<?> superclazz : superClasses) {
			Class<?>[] interfaces = superclazz.getInterfaces();
			if (interfaces.length > 0) {
				for (Class<?> interfaceClass : interfaces) {
					if (interfaceClass.isAnnotationPresent(annotation)) {
						return interfaceClass;
					}
				}
			}
		}
		return null;
	}
}
