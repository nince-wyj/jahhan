package net.jahhan.lmq.client.intf;

import net.jahhan.lmq.client.bean.LmqMessage;

/**
 * 采用token方式访问，需要处理token无效或过期的事件处理（客户端类会自己处理重连和重新申请token，业务使用方需要关注这两个事件的话可实现该接口处理）
 * Created by linwb on 2017/12/18 0018.
 */
public interface IMqttCallbackHandlerForToken extends IMqttCallbackHandler {

    /**
     * 收到token无效消息处理方法
     * @param clientId
     * @param groupId
     * @param deviceId
     * @param message
     */
    public void tokenInvalidHandler(String clientId, String groupId, String deviceId, LmqMessage message);

    /**
     * 收到token过期消息处理方法
     * @param clientId
     * @param groupId
     * @param deviceId
     * @param message
     */
    public void tokenExpireHandler(String clientId, String groupId, String deviceId, LmqMessage message);

}
