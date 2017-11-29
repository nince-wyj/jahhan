package net.jahhan.jdbc.constant.enumeration;

import lombok.Getter;

public enum DBConnectLevel {

	NONE(0), READ(1), WRITE(2), BATCH(3);

	@Getter
	private int level;

	DBConnectLevel(int level) {
		this.level = level;
	}

}
