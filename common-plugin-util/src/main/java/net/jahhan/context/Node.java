package net.jahhan.context;

import java.util.Map;
import java.util.UUID;

import lombok.Data;

@Data
public class Node {
	private String nodeId = UUID.randomUUID().toString();
	private String host;
	private String registHost;
	private Map<String, Integer> ports;
	private Map<String, Integer> registPorts;
	private Integer pid = 0;
}
