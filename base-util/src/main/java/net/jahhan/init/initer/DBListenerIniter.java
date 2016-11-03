package net.jahhan.init.initer;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.Injector;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.db.publish.EventPublisherManager;
import net.jahhan.demand.DBEventListener;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.utils.ClassScaner;

@InitAnnocation(isLazy = false, initSequence = 4000)
public class DBListenerIniter implements BootstrapInit {
	@Inject
	private Injector injector;

	@Override
	public void execute() {
		if (SysConfiguration.getUseSQLDB()) {
			String[] packages = new String[] { "net.jahhan.dblistener",
					SysConfiguration.getCompanyName() + ".dblistener" };
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
}
