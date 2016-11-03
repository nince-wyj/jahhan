package net.jahhan.zk;

import org.apache.curator.framework.CuratorFramework;

public interface ListenerHandler{
	public void executor(CuratorFramework client);
}
