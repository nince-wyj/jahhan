package net.jahhan.lmq.client.intf;

import net.jahhan.lmq.client.bean.LmqMessage;
import net.jahhan.lmq.common.define.MqTopic;

import java.util.List;

/**
 * Created by linwb on 2017/12/15 0015.
 */
public interface IMqttCallbackHandler {
    /**连接完成*/
    public void connectComplete(String clientId, String deviceId,String groupId, List<MqTopic> topicList,boolean reconnect);
    /**失去连接*/
    public void connectionLost(String clientId, String deviceId,String groupId, List<MqTopic> topicList, Throwable throwable);
    /**消息到达处理*/
    public void messageArrived(LmqMessage message);

    /**
     * 消息传送完成处理
     * @param message message的secondtopicName没有值
     * @param topics 可能为空
     * @param qosArr
     */
    public void deliveryComplete(LmqMessage message,String[] topics,int[] qosArr) ;
}
