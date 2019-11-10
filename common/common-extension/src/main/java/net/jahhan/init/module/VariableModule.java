package net.jahhan.init.module;

import java.util.List;

import com.google.inject.AbstractModule;

import net.jahhan.common.extension.annotation.GlobalVariable;
import net.jahhan.common.extension.annotation.ThreadVariable;
import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 1100)
public class VariableModule extends AbstractModule {

	@Override
	public void configure() {
		String[] packages = PackageUtil.packages("variable");
		List<String> classNameList = new ClassScaner().parse(packages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String className : classNameList) {
			try {
				Class clazz = classLoader.loadClass(className);
				ThreadVariable threadVariable = (ThreadVariable) clazz.getAnnotation(ThreadVariable.class);
				if (null != threadVariable) {
					bind(clazz).toInstance(clazz.newInstance());
					continue;
				}
				GlobalVariable globalVariable = (GlobalVariable) clazz.getAnnotation(GlobalVariable.class);
				if (null != globalVariable) {
					bind(clazz).toInstance(clazz.newInstance());
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			}
		}
	}

}
