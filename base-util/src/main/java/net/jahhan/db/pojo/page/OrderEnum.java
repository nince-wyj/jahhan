package net.jahhan.db.pojo.page;

public enum OrderEnum {

	CREATE_TIME_DESC("CREATE_TIME desc"), CREATE_TIME_ASC("CREATE_TIME asc");

	private final String order;

	private OrderEnum(String order) {
		this.order = order;
	}

	public String toString() {
		return order;
	}
}