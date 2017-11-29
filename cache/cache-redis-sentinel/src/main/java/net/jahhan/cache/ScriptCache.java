package net.jahhan.cache;

import java.util.HashMap;
import java.util.Map;

public class ScriptCache {
	private static Map<String, Script> scriptMap = new HashMap<>();

	public static Script getScript(String name) {
		return scriptMap.get(name);
	}

	public static void setScripts(Map<String, Script> scriptMap) {
		ScriptCache.scriptMap = scriptMap;
	}
}
