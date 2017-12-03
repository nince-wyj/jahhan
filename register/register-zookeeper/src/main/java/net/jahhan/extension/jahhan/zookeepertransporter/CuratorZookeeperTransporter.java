package net.jahhan.extension.jahhan.zookeepertransporter;

import javax.inject.Singleton;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.JahhanZookeeperTransporter;
import net.jahhan.zookeeper.ZookeeperClient;
import net.jahhan.zookeeper.curator.CuratorZookeeperClient;

@Extension("curator")
@Singleton
public class CuratorZookeeperTransporter implements JahhanZookeeperTransporter {

	public ZookeeperClient connect() {
		return new CuratorZookeeperClient();
	}

}
