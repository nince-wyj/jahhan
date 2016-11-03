package net.jahhan.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 接口信息缓存，用于异步保存接口
 * @author nince
 *
 */
public class ActionMapCache {
	private static ActionMapCache instance = new ActionMapCache();

	private ActionMapCache() {

	}

	public static ActionMapCache getInstance() {
		return instance;
	}

	private Map<String, Map<String, String>> actionMap = new HashMap<>();

	public Map<String, String> getAction(String actName) {
		return actionMap.get(actName);
	}

	public void setAction(String actName, Map<String, String> actionInfoMap) {
		actionMap.put(actName, actionInfoMap);
	}

	public Map<String, Map<String, String>> getActionMap() {
		return actionMap;
	}
}
