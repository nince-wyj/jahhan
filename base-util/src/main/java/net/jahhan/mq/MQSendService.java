package net.jahhan.mq;

public interface MQSendService {

	public void send(Topic topic, String messageType, Message message);
}
