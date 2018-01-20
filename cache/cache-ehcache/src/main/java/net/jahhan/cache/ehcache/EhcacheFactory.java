package net.jahhan.cache.ehcache;

import net.sf.ehcache.CacheManager;

public class EhcacheFactory {

	private static CacheManager cacheManager;
	
	public static CacheManager getCacheManager(){
		return EhcacheFactory.cacheManager;
	}
	
	public static void init(CacheManager cacheManager){
		EhcacheFactory.cacheManager = cacheManager;
	}
}
