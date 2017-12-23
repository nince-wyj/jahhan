package net.jahhan.init.initer.register;

import javax.inject.Inject;

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
