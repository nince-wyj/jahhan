package net.jahhan.lmq.client.intf;

import net.jahhan.lmq.common.define.MqTopic;

import java.util.List;

/**
 * token相关的接口定义
 * Created by linwb on 2017/12/16 0016.
 */
public interface ILmqToken {
    /**申请token*/
    public String applyToken(List<MqTopic> topicsList);
    /**获取本地储存的token*/
    public String getLocalToken();


}
