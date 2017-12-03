package net.jahhan.init.module;

import java.util.List;

import javax.inject.Named;

import com.frameworkx.annotation.Controller;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 4000)
public class ControllerBindModule extends AbstractModule {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void configure() {
		String[] packages = PackageUtil.packages("controller");
		List<String> classNameList = new ClassScaner().parse(packages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String className : classNameList) {
			try {
				Class clazz = classLoader.loadClass(className);
				Named named = (Named) clazz.getAnnotation(Named.class);
				Controller controller = (Controller) clazz.getAnnotation(Controller.class);
				if (null != named) {
					bind(clazz).in(Scopes.SINGLETON);
				}
				if (null != controller) {
					Class[] interfaces = clazz.getInterfaces();
					Class interfaceClass = interfaces[0];
					bind(interfaceClass).to(clazz).in(Scopes.SINGLETON);
				}
			} catch (ClassNotFoundException e) {
			}
		}
	}
}
