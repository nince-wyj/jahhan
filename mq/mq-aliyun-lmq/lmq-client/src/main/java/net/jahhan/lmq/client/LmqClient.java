package net.jahhan.lmq.client;

import net.jahhan.lmq.client.intf.IMqttCallbackHandler;
import net.jahhan.lmq.common.define.MqTopic;
import net.jahhan.lmq.common.define.MqTopicDefine;
import net.jahhan.lmq.common.define.PushOrder;
import net.jahhan.lmq.common.util.Tools;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 默认采用SSL连接，同步请求的客户端（业务方单一，所有 MQTT 客户端运行的环境可控，逻辑意义上都属于一个账号。）
 * Created by linwb on 2017/12/14 0014.
 */
public class LmqClient extends LmqTokenClient {

	private static Logger logger = LoggerFactory.getLogger(LmqClient.class);


	private String accessKey;
	private String secretKey;
	private String sign;
	private boolean cleanSession = false;

	public LmqClient() {
		init();
	}

	public LmqClient(boolean getOfflineMsgNow) {
		setGetOfflineMsgNow(getOfflineMsgNow);
		init();
	}
	public LmqClient( PushOrder pushOrder) {
		setGetOfflineMsgNow(true);
		setPushOrder(pushOrder);
		init();
	}
	public LmqClient(int maxPushNum) {
		this(maxPushNum,PushOrder.DESC);
	}

	public LmqClient(int maxPushNum, PushOrder pushOrder) {
		setGetOfflineMsgNow(true);
		setPushOrder(pushOrder);
		setMaxPushNum(maxPushNum);
		init();
	}

	protected void init() {
		setTokenClient(false);
		super.init();
		accessKey = MqTopicDefine.accessKey;
		secretKey = MqTopicDefine.secretKey;
		try {
			sign = Tools.macSignature(getGroupId(), MqTopicDefine.secretKey);
		} catch (InvalidKeyException e) {
			logger.error("macSignature invalid key error!!groupId:" + getGroupId() + " accessKey:" + accessKey
					+ " secretKey:" + secretKey, e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("macSignature error!!groupId:" + getGroupId() + " secretKey:" + secretKey, e);
		}
		logger.info("\ngroupId:{}\ncleanSession:{}\naccessKey:{}\nsecretKey:{}\nsign:{}",
				getGroupId(), cleanSession, accessKey, secretKey, sign);
	}

	/**
	 * 非token访问方式启动
	 * @param deviceId
	 * @param callbackHandler
	 * @param secondTopics
	 */
	public void start(String deviceId, IMqttCallbackHandler callbackHandler, MqTopic... secondTopics) {
		start(deviceId, null, callbackHandler, secondTopics);
	}

	protected MqttConnectOptions createConnectOptions() {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setUserName(accessKey);
		connOpts.setPassword(sign.toCharArray());
		connOpts.setCleanSession(cleanSession);
		connOpts.setKeepAliveInterval(90);
		connOpts.setAutomaticReconnect(true);
		return connOpts;
	}

}
