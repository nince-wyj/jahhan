package com.cwgj.lmq.client;

import net.jahhan.lmq.client.intf.ILmqToken;
import net.jahhan.lmq.common.define.MqTopic;

import java.util.List;

/**
 * Created by linwb on 2017/12/18 0018.
 */
public class CwgjLmqToken implements ILmqToken{
    /**
     * 申请token
     */
    @Override
    public String applyToken(List<MqTopic> topicsList) {
        return "LzMT+XLFl5uQ6bDU0o4vUiD1tyZUuRMsRtiCs0jC/89YfYNDKkKbsD8SHE+oBvCx+rR6voF7GWM55H57d740BlPOPRHFv12DaNPMNkLZ5JJCvKP80Xo0WTyueZHxmSH3RFhfnieKruPKbKeh+/kYqXEJ2FHc7ldOZl0a0KjmoLulztYlyCwpXZlCt6GFmJgm2JFY7PJwf/7OOSmUYIYFs9106wCS/4W9rwYT3hmQrXDOByS59CZxq3b6DZfVBcZSUMUnKM6C8UQ=";
    }

    /**
     * 获取本地储存的token
     */
    @Override
    public String getLocalToken() {
        return "LzMT+XLFl5uQ6bDU0o4vUiD1tyZUuRMsRtiCs0jC/89YfYNDKkKbsD8SHE+oBvCx+rR6voF7GWM55H57d740BlPOPRHFv12DaNPMNkLZ5JJCvKP80Xo0WTyueZHxmSH3RFhfnieKruPKbKeh+/kYqXEJ2FHc7ldOZl0a0KjmoLulztYlyCwpXZlCt6GFmJgm2JFY7PJwf/7OOSmUYIYFs9106wCS/4W9rwYT3hmQrXDOByS59CZxq3b6DZfVBcZSUMUnKM6C8UQ=";
    }
}
