package net.jahhan.lmq.common.define;

/**
 * MQ的topic,传输质量默认：QoS1
 * tokenExpireTime、tokenType和tokenPermission使用token方式鉴权访问的时候，这两个字段才有效
 * Created by linwb on 2017/12/15 0015.
 */
public class MqTopic {
	private String topicName;
	private QoS qos = QoS.QoS1;
	/** token过期时间，默认：2小时，需要注意的是申请token时设置的时间是绝对时间戳，即到某个时间点过期，就是当前的时间戳加上这两小时的毫秒值 */
	private long tokenExpireTime = 2 * 60 * 60 * 1000;
	/** token类型，默认是MQTT */
	private MqTokenType tokenType = MqTokenType.MQTT;

	/** token权限，默认是只读 */
	private MqTokenPermission tokenPermission = MqTokenPermission.R;

	public MqTopic(String topicName, QoS qos) {
		this.topicName = topicName;
		this.qos = qos;
	}

	public MqTopic(String topicName, QoS qos, MqTokenType tokenType, MqTokenPermission tokenPermission,
				   long tokenExpireTime) {
		this(topicName, qos);
		this.tokenType = tokenType;
		this.tokenPermission = tokenPermission;
		this.tokenExpireTime = tokenExpireTime;
	}

	public QoS getQos() {
		return qos;
	}

	public void setQos(QoS qos) {
		this.qos = qos;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public long getTokenExpireTime() {
		return tokenExpireTime;
	}

	public void setTokenExpireTime(long tokenExpireTime) {
		this.tokenExpireTime = tokenExpireTime;
	}

	public MqTokenType getTokenType() {
		return tokenType;
	}

	public void setTokenType(MqTokenType tokenType) {
		this.tokenType = tokenType;
	}

	public MqTokenPermission getTokenPermission() {
		return tokenPermission;
	}

	public void setTokenPermission(MqTokenPermission tokenPermission) {
		this.tokenPermission = tokenPermission;
	}

	@Override
	public String toString() {
		return "MqTopic{" +
				"topicName='" + topicName + '\'' +
				", qos=" + qos +
				", tokenExpireTime=" + tokenExpireTime +
				", tokenType=" + tokenType +
				", tokenPermission=" + tokenPermission +
				'}';
	}

}
