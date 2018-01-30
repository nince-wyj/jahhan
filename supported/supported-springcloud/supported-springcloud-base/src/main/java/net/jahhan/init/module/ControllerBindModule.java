package net.jahhan.init.module;

import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import net.jahhan.common.extension.annotation.Service;
import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 4000)
public class ControllerBindModule extends AbstractModule {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void configure() {
		String[] packages = PackageUtil.packages("service");
		List<String> classNameList = new ClassScaner().parse(packages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String className : classNameList) {
			try {
				Class clazz = classLoader.loadClass(className);
				Service service = (Service) clazz.getAnnotation(Service.class);
				if (null != service) {
					bind(clazz).in(Scopes.SINGLETON);
				}
			} catch (ClassNotFoundException e) {
			}
		}
	}
}
