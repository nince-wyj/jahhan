package net.jahhan.api;

import net.jahhan.web.UserEntity;

public abstract class RequestMessage {

	private String sessionId;

	private UserEntity userEntity;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public UserEntity getUserEntity() {
		return userEntity;
	}

	public void setUserEntity(UserEntity userEntity) {
		this.userEntity = userEntity;
	}
}