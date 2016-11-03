package net.jahhan.cache;

import java.util.HashMap;
import java.util.Map;

import net.jahhan.web.UserEntity;

public class UserEntityCache {
	private static UserEntityCache instance = new UserEntityCache();

	private UserEntityCache() {

	}

	public static UserEntityCache getInstance() {
		return instance;
	}

	private Map<String, UserEntity> userEntityMap = new HashMap<>();

	public UserEntity getUserEntity(String wsSessionId) {
		return userEntityMap.get(wsSessionId);
	}

	public void setUserEntity(String wsSessionId, UserEntity userEntity) {
		userEntityMap.put(wsSessionId, userEntity);
	}

	public void removeUserEntity(String wsSessionId) {
		userEntityMap.remove(wsSessionId);
	}
}
