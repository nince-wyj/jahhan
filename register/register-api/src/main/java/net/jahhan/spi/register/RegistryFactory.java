package net.jahhan.spi.register;

import net.jahhan.common.extension.annotation.SPI;
import net.jahhan.context.Node;
import net.jahhan.register.api.Registry;

@SPI("zookeeper")
public interface RegistryFactory {
	Registry getRegistry(Node node);
}