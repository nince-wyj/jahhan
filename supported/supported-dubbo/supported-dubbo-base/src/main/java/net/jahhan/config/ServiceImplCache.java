package net.jahhan.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceImplCache {
	private static ServiceImplCache instance = null;

	public static ServiceImplCache getInstance() {
		if (instance == null) {
			synchronized (ServiceImplCache.class) { 
				if (instance == null) {
					instance = new ServiceImplCache();
				}
			}
		}
		return instance;
	}
	
	private final Map<String, Class<?>> serviceMap = new ConcurrentHashMap<>();
	
	public void regist(String intefaceName, Class<?> refClass){
		serviceMap.put(intefaceName, refClass);
	}
	
	public Class<?> getRef(String intefaceName){
		return serviceMap.get(intefaceName);
	}
}
