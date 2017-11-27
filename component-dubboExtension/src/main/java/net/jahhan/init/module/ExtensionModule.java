package net.jahhan.init.module;

import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.tomcat.TomcatHttpBinder;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperTransporter;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.protocol.rest.RestProtocol;
import com.alibaba.dubbo.rpc.proxy.javassist.JavassistProxyFactory;
import com.google.inject.servlet.ServletModule;

import net.jahhan.dubbo.remoting.zookeeper.CuratorZookeeperTransporter;
import net.jahhan.dubbo.rpc.cluster.support.FrameWorkCluster;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 4100)
public class ExtensionModule extends ServletModule {
	@Override
	protected void configureServlets() {
		bind(Cluster.class).to(FrameWorkCluster.class);
		bind(ZookeeperTransporter.class).to(CuratorZookeeperTransporter.class);
		bind(Protocol.class).to(RestProtocol.class);
		bind(HttpBinder.class).to(TomcatHttpBinder.class);
		bind(ProxyFactory.class).to(JavassistProxyFactory.class);
	}
}
