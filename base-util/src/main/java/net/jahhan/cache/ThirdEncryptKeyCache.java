package net.jahhan.cache;

import java.util.HashMap;
import java.util.Map;

public class ThirdEncryptKeyCache {
	private static ThirdEncryptKeyCache instance = new ThirdEncryptKeyCache();

	private ThirdEncryptKeyCache() {

	}

	public static ThirdEncryptKeyCache getInstance() {
		return instance;
	}

	private Map<String, String> encryptKeyMap = new HashMap<>();

	public String getEncryptKey(String name) {
		return encryptKeyMap.get(name);
	}

	public void setEncryptKey(String name, String encryptKey) {
		encryptKeyMap.put(name, encryptKey);
	}
}
