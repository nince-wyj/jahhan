package net.jahhan.init.initer.jdbc;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.Injector;

import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.jdbc.publish.EventPublisherManager;
import net.jahhan.spi.DBEventListener;

@InitAnnocation(isLazy = false, initSequence = 4000)
public class DBListenerIniter implements BootstrapInit {
	@Inject
	private Injector injector;

	@Override
	public void execute() {
		String[] packages = PackageUtil.packages("dblistener");
		List<String> classNameList = new ClassScaner().parse(packages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String className : classNameList) {
			try {
				Class<?> clazz = classLoader.loadClass(className);
				if (DBEventListener.class.isAssignableFrom(clazz)) {
					EventPublisherManager.addListener((DBEventListener) injector.getInstance(clazz));
				}
			} catch (ClassNotFoundException e) {
			}
		}
	}
}
