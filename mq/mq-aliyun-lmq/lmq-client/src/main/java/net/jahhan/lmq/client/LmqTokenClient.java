package net.jahhan.lmq.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.io.netty.util.internal.StringUtil;
import net.jahhan.lmq.client.bean.LmqMessage;
import net.jahhan.lmq.client.intf.ILmqToken;
import net.jahhan.lmq.client.intf.IMqttCallbackHandler;
import net.jahhan.lmq.client.intf.IMqttCallbackHandlerForToken;
import net.jahhan.lmq.common.define.MqTopic;
import net.jahhan.lmq.common.define.MqTopicDefine;
import net.jahhan.lmq.common.define.QoS;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通过token验证的LMQ客户端（用户环境不可控，用户需要多维度验证场景，如移动端场景）
 * Created by linwb on 2017/12/16 0016.
 */
public class LmqTokenClient {

	private static Logger logger = LoggerFactory.getLogger(LmqTokenClient.class);

	private String brokerUrl;
	private String groupId;
	private MqTopic parentTopic;
	private MqTopic deviceTopic;
	private List<MqTopic> secondTopicsList;
	private String clientId;
	private String deviceId;
	private ILmqToken token;
	private boolean cleanSession = false;
	private MqttClient mqttClient;
	private IMqttCallbackHandler callbackHandler;
	private boolean isStarted;
	private boolean ssl = true;
	private boolean isTokenClient = true;

	public LmqTokenClient() {
		init();
	}

	public LmqTokenClient(boolean ssl) {
		this.ssl = ssl;
		init();
	}

	protected void init() {
		if (ssl) {
			brokerUrl = MqTopicDefine.sslBrokerUrl;
		} else {
			brokerUrl = MqTopicDefine.brokerUrl;
		}
		groupId = MqTopicDefine.groupId;
		parentTopic = MqTopicDefine.parentTopic;
		cleanSession = MqTopicDefine.cleanSession;
		logger.info("\nbrokerUrl:{}\ngroupId:{}\ntopic:{}\ncleanSession:{}", brokerUrl, groupId, parentTopic,
				cleanSession);
	}

	public void start(String deviceId, ILmqToken token, IMqttCallbackHandler callbackHandler, MqTopic... secondTopics) {
		if (this.isStarted) {
			return;
		}
		this.callbackHandler = callbackHandler;
		this.deviceId = deviceId;
		this.isStarted = true;
		this.clientId = groupId + "@@@" + deviceId;
		this.deviceTopic = new MqTopic(deviceId, QoS.QoS1);
		this.token = token;
		secondTopicsList = new ArrayList<>();
		if (secondTopics != null) {
			secondTopicsList.addAll(Arrays.asList(secondTopics));
		}

		List<MqTopic> allTopicList = new ArrayList<>();
		allTopicList.add(parentTopic);
		allTopicList.add(deviceTopic);
		allTopicList.addAll(secondTopicsList);

		for (MqTopic topic : allTopicList) {
			if (!topic.equals(parentTopic)) {
				topic.setTopicName(parentTopic.getTopicName() + "/" + topic.getTopicName());
			}
		}

		try {
			mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
			mqttClient.setCallback(createMqttCallbackExtended(allTopicList));
			logger.info("MqttClient({}) connecting...", clientId);
			IMqttToken mqttToken = mqttClient.connectWithResult(createConnectOptions());
			mqttToken.waitForCompletion();

//			if (isTokenClient) {// 不一定要执行
//				JSONObject object = new JSONObject();
//				object.put("token", token.getLocalToken());// body of token
//				object.put("type", MqTopicDefine.uploadTokenTopic.getTokenPermission());
//				MqttMessage message = new MqttMessage(object.toJSONString().getBytes());
//				message.setQos(MqTopicDefine.uploadTokenTopic.getQos().getValue());
//				MqttTopic pubTopic = mqttClient.getTopic(MqTopicDefine.uploadTokenTopic.getTopicName());
//				MqttDeliveryToken mqttDeliveryToken = pubTopic.publish(message);
//				mqttDeliveryToken.waitForCompletion();// sync wait
//				// Token upload ok , do normal sub and pub
//				subscribe(allTopicList);
//			}

		} catch (Exception e) {
			logger.error("start mqtt receiver error! clientId:" + clientId + " secondTopics:" + secondTopics, e);
		}
	}

	protected MqttConnectOptions createConnectOptions() {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		logger.info("Connecting to broker:()", brokerUrl);
		connOpts.setServerURIs(new String[] { brokerUrl });
		connOpts.setCleanSession(cleanSession);
		connOpts.setKeepAliveInterval(90);
		connOpts.setAutomaticReconnect(true);
		return connOpts;
	}

	private MqttCallbackExtended createMqttCallbackExtended(List<MqTopic> topicList) {
		return new MqttCallbackExtended() {
			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
				// when connect success,do sub topic
				logger.info("MqttClient id:" + clientId + " connect success!!!");
				try {
					if (isTokenClient) {
						JSONObject object = new JSONObject();
						object.put("token", token.getLocalToken());
						object.put("type", MqTopicDefine.uploadTokenTopic.getTokenPermission());
						MqttMessage message = new MqttMessage(object.toJSONString().getBytes());
						message.setQos(MqTopicDefine.uploadTokenTopic.getQos().getValue());
						mqttClient.publish(MqTopicDefine.uploadTokenTopic.getTopicName(), message);
					}

					if (!reconnect) {// 重新连接上不再订阅消息
						subscribe(topicList);
						logger.info("mqttclient({}) subscribe topic:{}", clientId, JSON.toJSONString(topicList));
					}
				} catch (Exception e) {
					logger.error("mqtt subscribe error! clientId:" + clientId + " topicFilterList:"
							+ JSON.toJSONString(topicList), e);
				}
				callbackHandler.connectComplete(clientId, deviceId, groupId, topicList, reconnect);
			}

			@Override
			public void connectionLost(Throwable throwable) {
				logger.error("MqttClient id:" + clientId + " connect lost!!!", throwable);
				callbackHandler.connectionLost(clientId, deviceId, groupId, topicList, throwable);
			}

			@Override
			public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
				logger.info("MqttClient({}) receive msg from topic:{} ,msg:{} body:{}", clientId, topic,
						JSON.toJSONString(mqttMessage), mqttMessage);
				if (topic.equals("$SYS/tokenInvalidNotice")) {
					// 重新申请token
					token.applyToken(topicList);

					if (callbackHandler instanceof IMqttCallbackHandlerForToken) {// 客户端不是传入IMqttCallbackHandlerForToken实现类，忽略处理事件
						((IMqttCallbackHandlerForToken) callbackHandler).tokenInvalidHandler(clientId, groupId,
								deviceId, new LmqMessage(groupId, clientId, deviceId, parentTopic.getTopicName(),
										topic, mqttMessage));
					}
				} else if (topic.equals("$SYS/tokenExpireNotice")) {
					// Token过期，重新申请token
					token.applyToken(topicList);

					if (callbackHandler instanceof IMqttCallbackHandlerForToken) {// 客户端不是传入IMqttCallbackHandlerForToken实现类，忽略处理事件
						((IMqttCallbackHandlerForToken) callbackHandler).tokenExpireHandler(clientId, groupId, deviceId,
								new LmqMessage(groupId, clientId, deviceId, parentTopic.getTopicName(),
										topic, mqttMessage));
					}

				} else {
					String topicName = topic;
					if (!topicName.equals(parentTopic.getTopicName())) {
						topicName = topicName.replace(parentTopic.getTopicName() + "/", "");
					}
					callbackHandler
							.messageArrived(new LmqMessage(groupId, clientId, deviceId, parentTopic.getTopicName(),
									topicName, mqttMessage));
				}

			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				logger.info("sender msg succeed");
				MqttMessage mqttMessage = null;
				try {
					mqttMessage = token.getMessage();
				} catch (Exception e) {
					e.printStackTrace();
				}
				callbackHandler
						.deliveryComplete(new LmqMessage(groupId, clientId, deviceId, parentTopic.getTopicName(),
								null, mqttMessage), token.getTopics(), token.getGrantedQos());
			}
		};
	}

	public void subscribe(List<MqTopic> topicList) throws Exception {
		if (topicList != null) {
			String[] topicArr = new String[topicList.size()];
			int[] qosArr = new int[topicList.size()];
			int index = 0;
			for (MqTopic topic : topicList) {
				topicArr[index] = topic.getTopicName();
				qosArr[index] = topic.getQos().getValue();
				index++;
			}
			mqttClient.subscribe(topicArr, qosArr);
		}

	}

	public void unsubscribe(MqTopic... topics) throws Exception {
		if (topics != null && topics.length > 0) {
			String[] topicArr = new String[topics.length];
			int index = 0;
			for (MqTopic topic : topics) {
				if (topic.equals(parentTopic)) {
					topicArr[index] = topic.getTopicName();
				} else {
					topicArr[index] = parentTopic.getTopicName() + "/" + topic.getTopicName();
				}
				index++;
			}
			mqttClient.unsubscribe(topicArr);
		}
	}

	/**
	 * 发布非滞留的信息
	 *
	 * @param topic
	 * @param msg
	 * @throws Exception
	 */
	public void publish(MqTopic topic, String msg) throws Exception {
		publish(topic, msg, false);
	}

	public void publish(MqTopic topic, String msg, Boolean retained) throws Exception {
		if (topic == null || StringUtil.isNullOrEmpty(msg)) {
			return;
		}
		boolean isRetained = false;
		if (retained != null) {
			isRetained = retained;
		}

		String topicName = parentTopic.equals(topic) ? topic.getTopicName()
				: parentTopic.getTopicName() + "/" + topic.getTopicName();

		mqttClient.publish(topicName, msg.getBytes(), topic.getQos().getValue(), isRetained);
	}

	public void reconect() throws Exception {
		mqttClient.reconnect();
	}

	public boolean isConnected() {
		return mqttClient.isConnected();
	}

	public void disconnect() throws Exception {
		mqttClient.disconnect();
	}

	public void close() throws Exception {
		mqttClient.close();
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public MqTopic getDeviceTopic() {
		return deviceTopic;
	}

	public void setDeviceTopic(MqTopic deviceTopic) {
		this.deviceTopic = deviceTopic;
	}

	public List<MqTopic> getSecondTopicsList() {
		return secondTopicsList;
	}

	public void setSecondTopicsList(List<MqTopic> secondTopicsList) {
		this.secondTopicsList = secondTopicsList;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public ILmqToken getToken() {
		return token;
	}

	public void setToken(ILmqToken token) {
		this.token = token;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean started) {
		isStarted = started;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public boolean isTokenClient() {
		return isTokenClient;
	}

	public void setTokenClient(boolean tokenClient) {
		isTokenClient = tokenClient;
	}
}
