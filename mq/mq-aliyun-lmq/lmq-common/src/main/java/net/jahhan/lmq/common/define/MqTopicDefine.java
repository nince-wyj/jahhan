package net.jahhan.lmq.common.define;

import net.jahhan.lmq.common.util.Tools;

import java.util.Properties;

/**
 * Created by linwb on 2017/12/15 0015.
 */
public class MqTopicDefine {
	/** 一级父topic */
	public static MqTopic parentTopic;
	/**上传token的topic*/
	public static MqTopic uploadTokenTopic;
	/**离线消息的topic*/
	public static MqTopic offlineMsgTopic;
	public static String groupId;
	public static String producerId;
	public static String accessKey;
	public static String secretKey;
    public static String brokerUrl;
	public static String sslBrokerUrl;
    public static boolean cleanSession;
	static {
		Properties properties = Tools.loadProperties();
		String topic = properties.getProperty("mq-topic");
		int qosLevel = Integer.parseInt(properties.getProperty("qos"));
		parentTopic = new MqTopic(topic, QoS.getQos(qosLevel));

		uploadTokenTopic = new MqTopic("$SYS/uploadToken", QoS.QoS1);
		uploadTokenTopic.setTokenPermission(MqTokenPermission.RW);

		offlineMsgTopic = new MqTopic("$SYS/getOfflineMsg", QoS.QoS1);

		groupId = properties.getProperty("mqtt-group-id");
		producerId = properties.getProperty("mq-producer-id");
		accessKey = properties.getProperty("accessKey");
		secretKey = properties.getProperty("secretKey");
        brokerUrl = properties.getProperty("brokerUrl");
		sslBrokerUrl = properties.getProperty("sslBrokerUrl");
        cleanSession = Boolean.parseBoolean(properties.getProperty("cleanSession"));
	}

	private MqTopicDefine() {
	}
}
