package net.jahhan.init.initer.register;

import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;

import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.context.BaseContext;
import net.jahhan.context.Node;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.register.api.Registry;
import net.jahhan.spi.register.RegistryFactory;

@InitAnnocation(isLazy = false, initSequence = 2100)
public class RegisterFactoryIniter implements BootstrapInit {
	@Inject
	private RegistryFactory registryFactory;

	@Override
	public void execute() {
		Properties property = PropertiesUtil.getProperties("base");
		if(BooleanUtils.toBoolean(property.getProperty("node.register.inuse", "true"))){
			Node node = BaseContext.CTX.getNode();
			Registry registry = registryFactory.getRegistry(node);
			registry.register();
			Runtime.getRuntime().addShutdownHook(new Thread("register-shutdown-hook") {
				public void run() {
					try {
						registry.unregister();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
	}
}
