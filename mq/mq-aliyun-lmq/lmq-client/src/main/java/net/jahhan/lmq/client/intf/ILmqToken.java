package net.jahhan.lmq.client.intf;

/**
 * token相关的接口定义
 * Created by linwb on 2017/12/16 0016.
 */
public interface ILmqToken {
    /**申请token*/
    public String applyToken();
    /**获取本地储存的token*/
    public String getLocalToken();


}
