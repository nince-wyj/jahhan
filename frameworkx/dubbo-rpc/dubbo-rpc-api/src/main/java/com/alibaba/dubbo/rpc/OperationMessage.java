package com.alibaba.dubbo.rpc;

import java.sql.Date;
import java.util.Map;

import com.alibaba.dubbo.rpc.Result;

import lombok.Data;

@Data
public class OperationMessage {
	private String remoteHost;
	private String interfaceName;// 接口名称
	private Map<String, String> attachments;
	private Map<String, Object> requestMap;
	private Object result;
	private String exceptionMsg;

	private Date requestTime = new Date(System.currentTimeMillis());
	private long usedMillisecond;

	public OperationMessage(String remoteHost, String interfaceName, Map<String, String> attachments,
			Map<String, Object> requestMap, Result result, String errorMessage, long usedMillisecond) {
		this.remoteHost = remoteHost;
		this.interfaceName = interfaceName;
		this.usedMillisecond = usedMillisecond;
		this.attachments = attachments;
		this.requestMap = requestMap;
		if (null != result) {
			this.result = result.getValue();
			if (null != result.getException()) {
				this.exceptionMsg = result.getException().getMessage();
			}
		}
		if (null != errorMessage) {
			this.exceptionMsg = errorMessage;
		}
	}
}