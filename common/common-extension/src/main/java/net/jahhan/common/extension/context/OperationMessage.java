package net.jahhan.common.extension.context;

import java.sql.Date;
import java.util.Map;

import lombok.Data;

@Data
public class OperationMessage {
	protected String remoteHost;
	protected String interfaceName;// 接口名称
	protected Map<String, String> attachments;
	protected Map<String, Object> requestMap;
	protected Object result;
	protected String exceptionMsg;

	protected Date requestTime = new Date(System.currentTimeMillis());
	protected long usedMillisecond;

}