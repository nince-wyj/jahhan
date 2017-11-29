package net.jahhan.zookeeper;

public interface ChildNodeListener {

	void childChanged(String path, String type, String value);

}
