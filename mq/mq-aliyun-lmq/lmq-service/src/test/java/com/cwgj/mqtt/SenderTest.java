package com.cwgj.mqtt;

import net.jahhan.lmq.common.define.MqTopic;
import net.jahhan.lmq.common.define.QoS;
import org.junit.Before;
import org.junit.Test;

import net.jahhan.lmq.sender.MqSendMqttSender;

/**
 * Created by linwb on 2017/12/14 0014.
 */
public class SenderTest {
	String deviceId1 = "1111111111";
	String deviceId2 = "222222222";
	String deviceId3 = "3333333333";
	MqSendMqttSender sender = null;

	@Before
	public void init() {
		sender = new MqSendMqttSender();
	}

	@Test
	public void test1() {
		sender.send2DeviceBySupportOffline(deviceId1,"1111111111111SupportOffline");

	}

    @Test
    public void test2() {
		sender.send2DeviceByNoSupportOffline(deviceId2,"222222222NoSupportOffline");
    }
    @Test
    public void test3() {
        sender.send(new MqTopic("0591", QoS.QoS1), "0591059105910591059105910591",true);
    }

	@Test
	public void testToken() {
		sender.send(new MqTopic(deviceId3, QoS.QoS1), "deviceId3 msg==》3333333333333",true);
//		for (int index=0;index<10;index++) {
//			sender.send(new MqTopic(deviceId1, QoS.QoS1), "msg==》"+index,true);
//		}

	}

	@Test
	public void testAll() {
		sender.send(null, "AllAllAllAllAllAllAll",false);
	}
}
