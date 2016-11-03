package net.jahhan.api;

import java.util.HashMap;
import java.util.Map;

import net.jahhan.context.ApplicationContext;
import net.jahhan.context.HeadMessage;
import net.jahhan.web.UserEntity;

public class RequestMessage {
	private String serviceName;// 接口名称

	private int appType;// APP类型

	private String verNo;// 版本号

	private String sign;// 签名

	private String token;// ws sessionId

	private String msg;//

	private String localAddr;

	private String modifySince;

	private String thirdName;

	private Object content;

	private Object defaultObject;

	private String eventKey;//用户微信eventKey

	public String getEventKey() {
		return eventKey;
	}

	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}

	public Object getDefaultObject() {
		return defaultObject;
	}

	public void setDefaultObject(Object defaultObject) {
		this.defaultObject = defaultObject;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public String getThirdName() {
		return thirdName;
	}

	public void setThirdName(String thirdName) {
		this.thirdName = thirdName;
	}

	public String getModifySince() {
		return modifySince;
	}

	public void setModifySince(String modifySince) {
		this.modifySince = modifySince;
	}

	private Map<String, Object> requestMap = new HashMap<String, Object>();

	private HeadMessage headMessage = new HeadMessage();

	public HeadMessage getHeadMessage() {
		return headMessage;
	}

	public void setHeadMessage(HeadMessage headMessage) {
		this.headMessage = headMessage;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public int getAppType() {
		return appType;
	}

	public void setAppType(int appType) {
		this.appType = appType;
	}

	public String getVerNo() {
		return verNo;
	}

	public void setVerNo(String verNo) {
		this.verNo = verNo;
	}

	public Map<String, Object> getRequestMap() {
		return requestMap;
	}

	public void setRequestMap(Map<String, Object> requestMap) {
		this.requestMap = requestMap;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getLocalAddr() {
		return localAddr;
	}

	public void setLocalAddr(String localAddr) {
		this.localAddr = localAddr;
	}

	public UserEntity getUserEntity() {
		return ApplicationContext.CTX.getUserEntity();
	}
}