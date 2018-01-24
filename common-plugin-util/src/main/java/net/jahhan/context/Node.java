package net.jahhan.context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import lombok.Data;

@Data
public class Node {
	private String nodeId;
	private String host;
	private String registHost;
	private Map<String, Integer> ports = new HashMap<>();
	private Map<String, Integer> registPorts = new HashMap<>();
	private Integer pid = 0;
	private final Map<Integer, ServletContext> contextMap = new ConcurrentHashMap<Integer, ServletContext>();

	private Node() {
		System.setProperty("nodeId", UUID.randomUUID().toString());
		nodeId = System.getProperty("nodeId");
	}

	private static Node instance = null;

	public static Node getInstance() {
		if (instance == null) {
			synchronized (Node.class) { 
				if (instance == null) {
					instance = new Node();
				}
			}
		}
		return instance;
	}
	
	public void addServletContext(int port, ServletContext servletContext) {
		contextMap.put(port, servletContext);
	}

	public void removeServletContext(int port) {
		contextMap.remove(port);
	}

	public ServletContext getServletContext(int port) {
		return contextMap.get(port);
	}
}
