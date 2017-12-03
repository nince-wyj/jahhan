package net.jahhan.extension.zookeeperTransporter;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient;
import com.alibaba.dubbo.remoting.zookeeper.zkclient.ZkclientZookeeperClient;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.ZookeeperTransporter;

@Extension("zkclient")
@Singleton
public class ZkclientZookeeperTransporter implements ZookeeperTransporter {

	public ZookeeperClient connect(URL url) {
		return new ZkclientZookeeperClient(url);
	}

}
