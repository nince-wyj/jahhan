package net.jahhan.extension.zookeeperTransporter;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient;
import com.alibaba.dubbo.remoting.zookeeper.curator.CuratorZookeeperClient;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.ZookeeperTransporter;

@Extension("curator")
@Singleton
public class CuratorZookeeperTransporter implements ZookeeperTransporter {

	public ZookeeperClient connect(URL url) {
		return new CuratorZookeeperClient(url);
	}

}
