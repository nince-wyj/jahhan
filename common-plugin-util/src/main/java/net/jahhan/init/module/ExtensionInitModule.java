package net.jahhan.init.module;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.api.Wrapper;
import net.jahhan.com.alibaba.dubbo.common.compiler.support.JavassistCompiler;
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
	private net.jahhan.com.alibaba.dubbo.common.compiler.Compiler compiler = new JavassistCompiler();
	public static boolean needRun = true;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void configure() {
		if (needRun) {
			String[] packages = PackageUtil.packages("extension");
			List<String> classNameList = new ClassScaner().parse(packages);
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Properties properties = PropertiesUtil.getProperties("extensionInit");
			Map<Class, List<Class>> unSetMap = new HashMap<>();
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

						// 默认配置
						String defaultPlugin = properties.getProperty(interfaceClass.getName());

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
