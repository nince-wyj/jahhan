package net.jahhan.zookeeper;

import java.util.List;

public interface ZookeeperClient {

	void create(String path, String data, boolean ephemeral);

	void create(String path, boolean ephemeral);

	void delete(String path);

	List<String> getChildren(String path);

	List<String> addChildListener(String path, ChildListener listener);
	
	void addChildNodeListener(String path, ChildNodeListener listener);

	void removeChildListener(String path, ChildListener listener);

	void addStateListener(StateListener listener);

	void removeStateListener(StateListener listener);

	boolean isConnected();

	void close();
}
