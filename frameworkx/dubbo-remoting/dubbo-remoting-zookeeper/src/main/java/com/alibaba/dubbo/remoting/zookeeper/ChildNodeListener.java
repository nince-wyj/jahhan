package com.alibaba.dubbo.remoting.zookeeper;

public interface ChildNodeListener {

	void childChanged(String path, String type, String value);

}
