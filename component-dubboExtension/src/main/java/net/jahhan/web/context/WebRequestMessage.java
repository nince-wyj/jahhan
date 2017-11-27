package net.jahhan.web.context;

import java.util.HashMap;
import java.util.Map;

import net.jahhan.api.RequestMessage;

public class WebRequestMessage extends RequestMessage {
	private String serviceName;// 接口名称

	private String sign;// 签名

	private String secretKey;// 秘钥

	private String content;// 请求的json字符串

	private String localAddr;

	private String eventKey;// 用户微信eventKey

	private Map<String, Object> requestMap = new HashMap<>();

	private Map<String, String> headMessage = new HashMap<>();

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLocalAddr() {
		return localAddr;
	}

	public void setLocalAddr(String localAddr) {
		this.localAddr = localAddr;
	}

	public String getEventKey() {
		return eventKey;
	}

	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}

	public Map<String, Object> getRequestMap() {
		return requestMap;
	}

	public void setRequestMap(Map<String, Object> requestMap) {
		this.requestMap = requestMap;
	}

	public Map<String, String> getHeadMessage() {
		return headMessage;
	}

	public void setHeadMessage(Map<String, String> headMessage) {
		this.headMessage = headMessage;
	}
	
}