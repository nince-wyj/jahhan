package net.jahhan.init.module;

import java.util.Set;

import com.google.inject.AbstractModule;

import net.jahhan.init.InitAnnocation;
import net.jahhan.utils.ScanUtils;

@InitAnnocation(isLazy = false, initSequence = 2000)
public class ServiceModule extends AbstractModule {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void configure() {
		Set<Class> classes = ScanUtils.findClassInPath(".+Service", "com/framework/service/");
		for (Class daoClass : classes) {
			String cacheName = "net.jahhan.service.impl." + daoClass.getSimpleName() + "Impl";
			try {
				Class<?> implClass = Class.forName(cacheName);
				bind(daoClass).to(implClass);
			} catch (Exception e) {
			}
		}
	}

}
