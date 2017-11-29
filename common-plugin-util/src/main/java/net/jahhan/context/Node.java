package net.jahhan.context;

import java.util.Map;

import lombok.Data;

@Data
public class Node {
	private String nodeId;
	private String host;
	private String registHost;
	private Map<String, Integer> ports;
	private Map<String, Integer> registPorts;
	private Integer pid = 0;
}
