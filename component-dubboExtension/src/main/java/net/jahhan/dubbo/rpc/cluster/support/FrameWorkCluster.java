package net.jahhan.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;

/**
 * 直连或失败转移，当出现失败，重试其它服务器，但重试会带来更长延迟。
 * 
 */
public class FrameWorkCluster implements Cluster{

	public final static String NAME = "frameWork";

	public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
		return new FrameWorkClusterInvoker<T>(directory);
	}

}
