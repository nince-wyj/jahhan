package net.jahhan.lmq.common.define;

/**
 * 消息传输的服务质量
 * Created by linwb on 2017/12/13 0013.
 */
public enum QoS {
	/** 最多分发一次 */
	QoS0(0),
	/** 至少达到一次 */
	QoS1(1),
	/** 仅分发一次 */
	QoS2(2);

	public int getValue() {
		return value;
	}

	private int value;

	QoS(int value) {
		this.value = value;
	}

	/**
	 * 获取QOS对象
	 * @param qos qos值
	 * @return qos 值不正确返回QoS1
	 */
	public static QoS getQos(int qos) {
		QoS q = QoS1;
		switch (qos) {
		case 0:
			q = QoS0;
			break;
		case 1:
			q = QoS1;
			break;
		case 2:
			q = QoS2;
			break;
		}
		return q;
	}

	/**
	 * 是否有效的QOS
	 * 
	 * @param qos
	 * @return
	 */
	public static boolean isValidQos(int qos) {
		if (QoS0.getValue() == qos || QoS1.getValue() == qos || QoS2.getValue() == qos) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
