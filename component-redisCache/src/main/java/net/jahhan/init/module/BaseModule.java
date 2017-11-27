package net.jahhan.init.module;

import java.util.Set;

import org.slf4j.Logger;

import com.google.inject.AbstractModule;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.init.InitAnnocation;
import net.jahhan.init.module.PluginSetModule;
import net.jahhan.utils.ScanUtils;

@InitAnnocation(isLazy = false, initSequence = 1300)
public class BaseModule extends AbstractModule {
	private final Logger logger = LoggerFactory.getInstance().getLogger(PluginSetModule.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void configure() {
		logger.info("scan dao now!");
		Set<Class> classes = ScanUtils.findClassInPath(".+Dao",
				SysConfiguration.getCompanyName().replace(".", "/") + "/dao/");
		for (Class daoClass : classes) {
			String cacheName = SysConfiguration.getCompanyName()
					+ ".dao.cache." + daoClass.getSimpleName() + "Cache";
			try {
				Class<?> implClass = Class.forName(cacheName);
				bind(daoClass).to(implClass);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Set<Class> frameworkClasses = ScanUtils.findClassInPath(".+Dao", "com/framework/dao/");
		for (Class daoClass : frameworkClasses) {
			String cacheName = "net.jahhan.dao.cache." + daoClass.getSimpleName() + "Cache";
			try {
				Class<?> implClass = Class.forName(cacheName);
				bind(daoClass).to(implClass);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
