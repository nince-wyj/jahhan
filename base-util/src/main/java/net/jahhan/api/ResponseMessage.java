package net.jahhan.api;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.jahhan.constant.SystemErrorCode;

public class ResponseMessage {
	private int errorCode = SystemErrorCode.SUCCESS;
	private Object responseMap = new HashMap<>();
	private static Map<?, ?> defaultContent = Collections.unmodifiableMap(new HashMap<>());
	private String messageInfo = SystemErrorCode.MESSAGE_SUCCESS;
	private String redirect;
	private ResponseFile responseFile;
	private Date lastModify;
	private Integer cache;

	public Date getLastModify() {
		return lastModify;
	}

	public void setLastModify(Date lastModify) {
		this.lastModify = lastModify;
	}

	public Integer getCache() {
		return cache;
	}

	public void setCache(Integer cache) {
		this.cache = cache;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public ResponseFile getResponseFile() {
		return responseFile;
	}

	public void setResponseFile(ResponseFile responseFile) {
		this.responseFile = responseFile;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public Object getResponseMap() {
		return responseMap;
	}

	public void setResponseMap(Object responseMap) {
		if (responseMap == null || responseMap.equals("")) {
			this.responseMap = defaultContent;
		} else {
			this.responseMap = responseMap;
		}
	}

	@SuppressWarnings("unchecked")
	public void setResponseMapFix(Object responseMap) {
		if (responseMap instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) responseMap;
			if (map.containsKey("r_code")) {
				setErrorCode((Integer) map.get("r_code"));
			}

			if (map.containsKey("r_msg")) {
				setMessageInfo((String) map.get("r_msg"));
			}
		}

		this.responseMap = responseMap;
	}

	public String getMessageInfo() {
		return messageInfo;
	}

	public void setMessageInfo(String messageInfo) {
		this.messageInfo = messageInfo;
	}

}