package net.jahhan.jdbc.dopage;

public enum RangeEnum {

	CLOSE_OPEN("close_open"), CLOSE_CLOSE("close_close"), OPEN_CLOSE("open_close"), OPEN_OPEN("open_open");

	private final String rangeType;

	private RangeEnum(String rangeType) {
		this.rangeType = rangeType;
	}

	public String typeString() {
		return rangeType;
	}
}