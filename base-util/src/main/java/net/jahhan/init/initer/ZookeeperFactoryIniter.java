package net.jahhan.init.initer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.context.BaseContext;
import net.jahhan.demand.HostDemand;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.utils.ClassScaner;
import net.jahhan.utils.LocalIpUtils;
import net.jahhan.zk.ListenerHandler;
import net.jahhan.zk.ZKHostFactory;

@InitAnnocation(isLazy = false, initSequence = 2800)
public class ZookeeperFactoryIniter implements BootstrapInit {
	private final Logger logger = LoggerFactory.getInstance().getLogger(ZookeeperFactoryIniter.class);

	@Override
	public void execute() {
		String thisHost = "";
		HostDemand hostManager = BaseContext.CTX.getHostManager();
		if (null != hostManager) {
			thisHost = hostManager.getThisHostAndPort();
		} else {
			thisHost = LocalIpUtils.getFirstIp();
		}
		ZKHostFactory zookeeperFactory = new ZKHostFactory(SysConfiguration.getZkRegistNamespace(),
				SysConfiguration.getZkHost(), SysConfiguration.getApplicationType(), "/" + thisHost);
		List<ListenerHandler> listenerList = new ArrayList<>();
		String[] packages = new String[] { SysConfiguration.getCompanyName() + ".zk.listener",
				"net.jahhan.zk.listener" };
		List<String> classNameList = new ClassScaner().parse(packages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String className : classNameList) {
			try {
				Class<?> clazz = classLoader.loadClass(className);
				if (ListenerHandler.class.isAssignableFrom(clazz)) {
					ListenerHandler listenerHandler = (ListenerHandler) clazz.newInstance();
					listenerList.add(listenerHandler);
				}
			} catch (Exception e) {
				logger.error("zk监听失败:" + className, e);
			}
		}
		zookeeperFactory.setListeners(listenerList);
		try {
			zookeeperFactory.init();
		} catch (Exception e) {
			logger.error("zk启动失败", e);
		}
	}
}
