package net.jahhan.extension.zookeepertransporter;

import javax.inject.Singleton;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.ZookeeperTransporter;
import net.jahhan.zookeeper.ZookeeperClient;
import net.jahhan.zookeeper.curator.CuratorZookeeperClient;

@Extension("curator")
@Singleton
public class CuratorZookeeperTransporter implements ZookeeperTransporter {

	public ZookeeperClient connect() {
		return new CuratorZookeeperClient();
	}

}
