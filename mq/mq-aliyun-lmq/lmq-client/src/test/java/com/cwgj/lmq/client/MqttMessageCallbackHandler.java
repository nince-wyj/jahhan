package com.cwgj.lmq.client;

import com.alibaba.fastjson.JSON;
import net.jahhan.lmq.client.bean.LmqMessage;
import net.jahhan.lmq.client.intf.IMqttCallbackHandler;
import net.jahhan.lmq.common.define.MqTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by linwb on 2017/12/15 0015.
 */
public class MqttMessageCallbackHandler implements IMqttCallbackHandler {
    private static Logger logger = LoggerFactory.getLogger(MqttMessageCallbackHandler.class);
    /**
     * 连接完成
     *
     * @param clientId
     * @param deviceId
     * @param groupId
     * @param topicList
     * @param reconnect
     */
    @Override
    public void connectComplete(String clientId, String deviceId, String groupId, List<MqTopic> topicList, boolean reconnect) {
        logger.info("\nclientId:{}\ndeviceId:{}\ngroupId:{}\ntopicLis:{}\nreconnect:{}\n", clientId,  deviceId,  groupId, JSON.toJSONString(topicList),  reconnect);
    }

    /**
     * 失去连接
     *
     * @param clientId
     * @param deviceId
     * @param groupId
     * @param topicList
     * @param throwable
     */
    @Override
    public void connectionLost(String clientId, String deviceId, String groupId, List<MqTopic> topicList, Throwable throwable) {
        logger.info("\nclientId:{}\ndeviceId:{}\ngroupId:{}\ntopicLis:{}\nException:{}", clientId,  deviceId,  groupId, JSON.toJSONString(topicList),throwable.getMessage());
    }

    /**
     * 消息到达处理
     *
     * @param message
     */
    @Override
    public void messageArrived(LmqMessage message) {
        logger.info("messageArrived:{}",JSON.toJSONString(message));
    }

    /**
     * 消息传送完成处理
     *
     * @param message message的secondtopicName没有值
     * @param topics  可能为空
     * @param qosArr
     */
    @Override
    public void deliveryComplete(LmqMessage message, String[] topics, int[] qosArr) {
        logger.info("deliveryComplete message:{} topics:{}  qos:{}",JSON.toJSONString(message),topics==null?"":topics.toString(),qosArr==null?"":qosArr.toString());
    }
}
