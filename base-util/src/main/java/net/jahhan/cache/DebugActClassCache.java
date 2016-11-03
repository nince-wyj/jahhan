package net.jahhan.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * debug模式用于保存接口类信息的缓存
 * @author nince
 *
 */
public class DebugActClassCache {
	private static DebugActClassCache instance = new DebugActClassCache();

	private DebugActClassCache() {

	}

	public static DebugActClassCache getInstance() {
		return instance;
	}

	private Map<String, String> serviceIdMap = new HashMap<>();

	public String getClassName(String serviceId) {
		return serviceIdMap.get(serviceId);
	}

	public String setAct(String serviceId, String className) {
		return serviceIdMap.put(serviceId, className);
	}
}
