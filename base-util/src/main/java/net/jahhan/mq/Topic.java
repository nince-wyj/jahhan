package net.jahhan.mq;

/**
 * @author nince
 */
public enum Topic {
	SYSTEM("system."), // 系统处理消息，不发往客户端
	MESSAGE("message."), // 系统消息，发往客户端显示
	CHART("chart."), // 用户聊天消息
	HANDLE("handle.");// 需客户端处理的消息
	
	String parentType;

	Topic(String parentType) {
		this.parentType = parentType;
	}

	public String getParentType() {
		return parentType;
	}
}
