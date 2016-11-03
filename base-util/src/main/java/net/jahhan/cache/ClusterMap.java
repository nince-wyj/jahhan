package net.jahhan.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClusterMap {
	private static ClusterMap instance = new ClusterMap();

	private ClusterMap() {

	}

	public static ClusterMap getInstance() {
		return instance;
	}

	private Map<String, List<String>> serverMap = new ConcurrentHashMap<>();

	public void addServer(String cluster, String server) {
		List<String> serverList = null;
		if (!serverMap.containsKey(cluster)) {
			serverList = new ArrayList<>();
		} else {
			serverList = serverMap.get(cluster);
		}
		serverList.add(server);
		serverMap.put(cluster, serverList);
	}

	public void deleteServer(String cluster, String server) {
		if (serverMap.containsKey(cluster)) {
			List<String> serverList = serverMap.get(cluster);
			serverList.remove(server);
			serverMap.put(cluster, serverList);
		}
	}

	public Map<String, List<String>> getServerMap() {
		return serverMap;
	}
}
