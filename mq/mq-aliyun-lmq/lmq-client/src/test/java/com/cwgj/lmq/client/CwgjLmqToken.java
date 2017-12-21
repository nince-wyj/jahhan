package com.cwgj.lmq.client;

import net.jahhan.lmq.client.intf.ILmqToken;

/**
 * Created by linwb on 2017/12/18 0018.
 */
public class CwgjLmqToken implements ILmqToken{
    /**
     * 申请token
     */
    @Override
    public String applyToken() {
        return "LzMT+XLFl5uQ6bDU0o4vUiD1tyZUuRMsRtiCs0jC/89YfYNDKkKbsD8SHE+oBvCx+rR6voF7GWM55H57d740BjKT1tDilvDeivzAvK3DAAZCvKP80Xo0WTyueZHxmSH3RFhfnieKruPZwlBbItkA1mCDJ0SbxKYGsu3hQjDuB2m0PMvMrGW0NaBzoigbGwGH+vMQMgj+4tUVUKikaA+/ETXhvp7DrX6mM5AZ1eySEDo=";
    }

    /**
     * 获取本地储存的token
     */
    @Override
    public String getLocalToken() {
        return "LzMT+XLFl5uQ6bDU0o4vUiD1tyZUuRMsRtiCs0jC/89YfYNDKkKbsD8SHE+oBvCx+rR6voF7GWM55H57d740BjKT1tDilvDeivzAvK3DAAZCvKP80Xo0WTyueZHxmSH3RFhfnieKruPZwlBbItkA1mCDJ0SbxKYGsu3hQjDuB2m0PMvMrGW0NaBzoigbGwGH+vMQMgj+4tUVUKikaA+/ETXhvp7DrX6mM5AZ1eySEDo=";
    }
}
