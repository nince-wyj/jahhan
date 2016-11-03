package net.jahhan.init.module;

import java.util.List;

import javax.inject.Named;

import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

import net.jahhan.annotation.Job;
import net.jahhan.init.InitAnnocation;
import net.jahhan.utils.ClassScaner;
import net.jahhan.web.action.annotation.ActionService;

@InitAnnocation(isLazy = false, initSequence = 4000)
public class CosumeBindModule extends ServletModule {
	@Override
	protected void configureServlets() {
		String scanPath = ConfigUtils.getProperty("dubbo.annotation.package");
		List<String> classNameList = new ClassScaner().parse(scanPath.split(","));
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String className : classNameList) {
			try {
				Class<?> clazz = classLoader.loadClass(className);
				Named named = (Named) clazz.getAnnotation(Named.class);
				Job job = (Job) clazz.getAnnotation(Job.class);
				ActionService actionService = (ActionService) clazz.getAnnotation(ActionService.class);
				if (null != named || null != actionService || null != job) {
					bind(clazz).in(Scopes.SINGLETON);
				}
			} catch (ClassNotFoundException e) {
			}
		}
	}
}
