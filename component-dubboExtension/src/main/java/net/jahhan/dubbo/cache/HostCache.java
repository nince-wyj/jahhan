package net.jahhan.dubbo.cache;

import java.util.HashMap;
import java.util.Map;

public class HostCache {
	private static HostCache instance = new HostCache();

	private HostCache() {

	}

	public static HostCache getInstance() {
		return instance;
	}

	private Map<String, String> hostMap = new HashMap<>();

	private int port = 0;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost(String interfaceName) {
		return hostMap.get(interfaceName);
	}

	public void setHost(String interfaceName, String host) {
		hostMap.put(interfaceName, host);
	}
}
