package net.jahhan.service.service.constant;

import lombok.Getter;

public enum UserTokenType {
	OPEN_TOKEN("OPEN_TOKEN"), SINGLE_TOKEN("SINGLE_TOKEN"), BEARER_TOKEN("BEARER_TOKEN"), DEBUG("DEBUG");

	@Getter
	private String value;

	UserTokenType(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return value;
	}
}
