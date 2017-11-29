package net.jahhan.register;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import net.jahhan.context.Node;

@Singleton
public class ClusterMessageHolder {

	private Map<String, Map<String, Node>> serviceMap = new ConcurrentHashMap<>();

	public void addServer(String service, String nodeId, Node value) {
		Map<String, Node> serverMap = null;
		if (!serviceMap.containsKey(service)) {
			serverMap = new ConcurrentHashMap<>();
		} else {
			serverMap = serviceMap.get(service);
		}
		serverMap.put(nodeId, value);
		serviceMap.put(service, serverMap);
	}

	public void deleteServer(String service, String nodeId) {
		if (serviceMap.containsKey(service)) {
			Map<String, Node> serverMap = serviceMap.get(service);
			serverMap.remove(nodeId);
			serviceMap.put(service, serverMap);
		}
	}

	public Map<String, Map<String, Node>> getServiceMap() {
		return serviceMap;
	}

	public boolean contains(String nodeId) {
		Set<String> keySet = serviceMap.keySet();
		for (String service : keySet) {
			Map<String, Node> map = serviceMap.get(service);
			if (map.containsKey(nodeId)) {
				return true;
			}
		}
		return false;
	}
}
