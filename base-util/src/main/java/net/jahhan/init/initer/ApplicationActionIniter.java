package net.jahhan.init.initer;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.google.inject.Injector;

import net.jahhan.api.Action;
import net.jahhan.cache.DebugActClassCache;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.utils.ClassScaner;
import net.jahhan.web.action.ServiceRegisterHelper;
import net.jahhan.web.action.annotation.ActionService;

@InitAnnocation(isLazy = false, initSequence = 9000, onlyWeb = true)
public class ApplicationActionIniter implements BootstrapInit {
	private final Logger logger = LoggerFactory.getInstance().getLogger(ApplicationActionIniter.class);
	@Inject
	private Injector injector;
	@Inject
	private ServiceRegisterHelper serviceRegisterManager;

	@Override
	public void execute() {
		String[] packages = new String[] { SysConfiguration.getCompanyName() + ".action", "net.jahhan.common.action",
				"net.jahhan.manager.action" };
		parse(packages);
	}

	/**
	 * 接口初始化
	 * 
	 * @param packageNames
	 */
	private void parse(final String[] packageNames) {
		List<String> classNameList = new ClassScaner().parse(packageNames);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (!SysConfiguration.getIsDebug()) {
			for (String className : classNameList) {
				serviceRegisterManager.registerService(classLoader, injector, className);
			}
		} else {
			for (String className : classNameList) {
				try {
					Class<?> clazz = classLoader.loadClass(className);
					if (clazz.isAnnotationPresent(ActionService.class) && Action.class.isAssignableFrom(clazz)) {
						ActionService actionService = clazz.getAnnotation(ActionService.class);
						if (DebugActClassCache.getInstance().setAct(actionService.act(), className) != null) {
							logger.error("接口服务重复:" + actionService.act());
							Thread.sleep(1500);
							System.exit(-1);
						}
					}
				} catch (Exception ex) {
					logger.error("接口服务启动失败:" + className, ex);
				}
			}
		}
	}
}
