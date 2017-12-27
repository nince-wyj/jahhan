package net.jahhan.context;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Node {
	private String nodeId = System.getProperty("nodeId");
	private String host;
	private String registHost;
	private Map<String, Integer> ports = new HashMap<>();
	private Map<String, Integer> registPorts = new HashMap<>();
	private Integer pid = 0;
}
