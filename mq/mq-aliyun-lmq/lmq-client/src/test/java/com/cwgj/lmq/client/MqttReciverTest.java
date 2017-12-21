package com.cwgj.lmq.client;

import net.jahhan.lmq.client.LmqClient;
import net.jahhan.lmq.client.LmqTokenClient;
import net.jahhan.lmq.common.define.MqTopic;
import net.jahhan.lmq.common.define.MqTopicDefine;
import net.jahhan.lmq.common.define.QoS;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by linwb on 2017/12/14 0014.
 */
public class MqttReciverTest {
	String deviceId1 = "1111111111";
	String deviceId2 = "222222222";
	LmqClient lmqClient = null;
	LmqTokenClient tokenClient = null;
	MqttMessageCallbackHandler callbackHandler = null;
    MqttMessageCallbackHandlerForToken callbackHandlerForToken=null;
	@Before
	public void init() {
//		lmqClient = new LmqClient();
		tokenClient = new LmqTokenClient(false);
		callbackHandler = new MqttMessageCallbackHandler();
		callbackHandlerForToken=new MqttMessageCallbackHandlerForToken();
	}

	@Test
	public void test1() {
		lmqClient.start(deviceId1, callbackHandler);
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test2() throws Exception {
		lmqClient.start(deviceId2, callbackHandler, new MqTopic("0591", QoS.QoS1));
		lmqClient.publish(new MqTopic(deviceId1,QoS.QoS1),"come from deviceId2!!!");
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testToken() throws Exception {
		tokenClient.start(MqTopicDefine.parentTopic.getTopicName(),new CwgjLmqToken(), callbackHandlerForToken, new MqTopic("0591", QoS.QoS1));
//		tokenClient.publish(new MqTopic(deviceId1,QoS.QoS1),"======come from token test!!!=======");
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
