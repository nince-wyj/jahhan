package com.cwgj.lmq.client;

import com.alibaba.fastjson.JSON;
import net.jahhan.lmq.client.bean.LmqMessage;
import net.jahhan.lmq.client.intf.IMqttCallbackHandlerForToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by linwb on 2017/12/18 0018.
 */
public class MqttMessageCallbackHandlerForToken extends MqttMessageCallbackHandler
		implements IMqttCallbackHandlerForToken {
	private static Logger logger = LoggerFactory.getLogger(MqttMessageCallbackHandler.class);

	/**
	 * 收到token无效消息处理方法
	 *
	 * @param clientId
	 * @param groupId
	 * @param deviceId
	 * @param message
	 */
	@Override
	public void tokenInvalidHandler(String clientId, String groupId, String deviceId, LmqMessage message) {
		logger.info("##tokenInvalidHandler:\nclientId:{}\ngroupId:{}\ndeviceId:{}\nmessage:{}", clientId, groupId, deviceId,
				JSON.toJSONString(message));
	}

	/**
	 * 收到token过期消息处理方法
	 *
	 * @param clientId
	 * @param groupId
	 * @param deviceId
	 * @param message
	 */
	@Override
	public void tokenExpireHandler(String clientId, String groupId, String deviceId, LmqMessage message) {
        logger.info("###tokenExpireHandler:\nclientId:{}\ngroupId:{}\ndeviceId:{}\nmessage:{}", clientId, groupId, deviceId,
                JSON.toJSONString(message));
	}
}
