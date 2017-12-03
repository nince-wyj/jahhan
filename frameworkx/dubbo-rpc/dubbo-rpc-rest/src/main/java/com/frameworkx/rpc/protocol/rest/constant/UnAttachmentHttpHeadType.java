package com.frameworkx.rpc.protocol.rest.constant;

import java.util.ArrayList;
import java.util.List;

public enum UnAttachmentHttpHeadType {
	HOST("host"), CONNECTION("connection"), USER_AGENT("user-agent"), CONTENT_TYPE("content-type"), ACCEPT(
			"accept"), ACCEPT_ENCODING("accept-encoding"), ACCEPT_LANGUAGE("accept-language");

	private String headKey;

	UnAttachmentHttpHeadType(String headKey) {
		this.headKey = headKey;
	}

	static List<String> unAttachmentHttpHeadTypes;

	static {
		unAttachmentHttpHeadTypes = new ArrayList<>();
		UnAttachmentHttpHeadType[] values = UnAttachmentHttpHeadType.values();
		for (int i = 0; i < values.length; i++) {
			unAttachmentHttpHeadTypes.add(values[i].headKey);
		}
	}

	public static List<String> getUnAttachmentHttpHeadType() {
		return unAttachmentHttpHeadTypes;
	}
}
