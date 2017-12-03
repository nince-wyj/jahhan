package com.frameworkx.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public class ClusterMessageHolder {

	private Map<String, Map<String, String>> serviceMap = new ConcurrentHashMap<>();

	public void addServer(String service, String nodeId, String value) {
		Map<String, String> serverMap = null;
		if (!serviceMap.containsKey(service)) {
			serverMap = new HashMap<>();
		} else {
			serverMap = serviceMap.get(service);
		}
		serverMap.put(nodeId, value);
		serviceMap.put(service, serverMap);
	}

	public void deleteServer(String service, String nodeId) {
		if (serviceMap.containsKey(service)) {
			Map<String, String> serverMap = serviceMap.get(service);
			serverMap.remove(nodeId);
			serviceMap.put(service, serverMap);
		}
	}

	public Map<String, Map<String, String>> getServiceMap() {
		return serviceMap;
	}

	public boolean contains(String nodeId) {
		Set<String> keySet = serviceMap.keySet();
		for (String service : keySet) {
			Map<String, String> map = serviceMap.get(service);
			if (map.containsKey(nodeId)) {
				return true;
			}
		}
		return false;
	}
}
