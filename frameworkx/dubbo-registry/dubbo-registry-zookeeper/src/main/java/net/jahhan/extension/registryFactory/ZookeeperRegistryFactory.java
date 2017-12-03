package net.jahhan.extension.registryFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;
import com.frameworkx.annotation.Adaptive;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.context.BaseContext;
import net.jahhan.spi.ZookeeperTransporter;

@Extension("zookeeper")
@Singleton
public class ZookeeperRegistryFactory extends AbstractRegistryFactory {
	
	@Inject
	@Adaptive
	private ZookeeperTransporter zookeeperTransporter;

	public Registry createRegistry(URL url) {
        ZookeeperRegistry zookeeperRegistry = new ZookeeperRegistry(url, zookeeperTransporter);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
            	zookeeperRegistry.getZkClient().delete("/service/" + BaseConfiguration.SERVICE + "/" + BaseContext.CTX.getNode().getNodeId());
            }
        }, "DubboShutdownHook-zookeeper"));
		return zookeeperRegistry;
    }

}