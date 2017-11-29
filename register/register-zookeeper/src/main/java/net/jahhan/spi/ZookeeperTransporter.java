package net.jahhan.spi;

import net.jahhan.common.extension.annotation.SPI;
import net.jahhan.zookeeper.ZookeeperClient;

@SPI("zkclient")
public interface ZookeeperTransporter {

	ZookeeperClient connect();

}
