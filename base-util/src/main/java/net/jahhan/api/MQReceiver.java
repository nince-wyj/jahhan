package net.jahhan.api;

import net.jahhan.mq.Message;

/**
 * 消息队列
 * 
 * @author nince
 *
 */
public interface MQReceiver {

	public void listen(Message message);
}
