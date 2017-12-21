package net.jahhan.lmq.client.bean;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by linwb on 2017/12/15 0015.
 */
public class LmqMessage extends MqttMessage {
	private String groupId;
	private String clientId;
	private String deviceId;
	private String parentTopicName;
	private String secondtopicName;

	public LmqMessage() {
	}

	public LmqMessage(String groupId, String clientId, String deviceId, String parentTopicName, String secondtopicName,
			MqttMessage mqttMessage) {
		this(groupId, clientId, deviceId, parentTopicName, secondtopicName);
		if (mqttMessage != null) {
			this.setDuplicate(mqttMessage.isDuplicate());
			this.setId(mqttMessage.getId());
			this.setPayload(mqttMessage.getPayload());
			this.setQos(mqttMessage.getQos());
			this.setRetained(mqttMessage.isRetained());
		}
	}

	public LmqMessage(String groupId, String clientId, String deviceId, String parentTopicName,
			String secondtopicName) {
		this.groupId = groupId;
		this.clientId = clientId;
		this.deviceId = deviceId;
		this.parentTopicName = parentTopicName;
		this.secondtopicName = secondtopicName;
	}

	public String getContent() {
		byte[] data = getPayload();
		if (data != null) {
			return new String(data);
		}
		return null;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
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

	public String getParentTopicName() {
		return parentTopicName;
	}

	public void setParentTopicName(String parentTopicName) {
		this.parentTopicName = parentTopicName;
	}

	public String getSecondtopicName() {
		return secondtopicName;
	}

	public void setSecondtopicName(String secondtopicName) {
		this.secondtopicName = secondtopicName;
	}
}
