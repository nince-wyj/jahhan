package net.jahhan.cache;

import lombok.Data;

@Data
public class Script {
	private String sha;
	private String name;
	private String luaScript;
}
