package net.jahhan.cache.mq;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisConstants;
import net.jahhan.cache.RedisFactory;
import net.jahhan.mq.MQSendService;
import net.jahhan.mq.Message;
import net.jahhan.mq.Topic;
import net.jahhan.utils.JsonUtil;

@Singleton
public class MqProConSender implements MQSendService {
	protected Logger logger = LoggerFactory.getLogger(MqProConSender.class);

	private static Redis getRedis() {
		return RedisFactory.getRedis(RedisConstants.TABLE_MQ, null);
	}

	private static String mqIdPre = "MQ_ID_SEQ:";

	@Override
	public void send(Topic topic, String messageType, Message message) {
		message.setId(getRedis().incr(mqIdPre + topic.getParentType() + messageType));
		Long result = getRedis().push(topic.getParentType() + messageType, JsonUtil.toJson(message));
		logger.debug(
				topic.getParentType() + messageType + " message:" + JsonUtil.toJson(message) + " result:" + result);
	}

}
