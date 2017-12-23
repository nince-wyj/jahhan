package net.jahhan.lmq.client.intf;

import net.jahhan.lmq.common.define.MqTopic;

import java.util.List;

/**
 * token相关的接口定义
 * Created by linwb on 2017/12/16 0016.
 */
public interface ILmqToken {
	/**
	 * 申请token(客户端订阅的所有主题按R、W、RW三种权限归类后申请token，然后要操作哪个主题使用相应的token请求)
	 */
	public String applyToken(List<MqTopic> topicsList);

	/** 获取本地储存的token */
	public String getLocalToken();

}
