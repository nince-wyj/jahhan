package net.jahhan.init.initer;

import java.util.List;

import com.alibaba.dubbo.config.annotation.Service;

import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.config.ServiceImplCache;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 1600)
public class DubboIniter implements BootstrapInit {

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
			Service controller = (Service) scanClass.getAnnotation(Service.class);
			Class<?>[] interfaces = scanClass.getInterfaces();
			if (null != controller && interfaces.length == 1) {
				ServiceImplCache.getInstance().regist(scanClass.getInterfaces()[0].getName(), scanClass);
			}

		}
	}
}
