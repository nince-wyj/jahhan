package net.jahhan.factory.httpclient;

import net.jahhan.api.HttpCallBack;
import net.jahhan.context.InvocationContext;

public class PipeLineCallbackHold {
	private String path;
	private HttpCallBack httpCallBack;
	private InvocationContext invocationContext;

	public PipeLineCallbackHold(String path, HttpCallBack httpCallBack, InvocationContext invocationContext) {
		super();
		this.path = path;
		this.httpCallBack = httpCallBack;
		this.invocationContext = invocationContext;
	}

	public String getPath() {
		return path;
	}

	public HttpCallBack getHttpCallBack() {
		return httpCallBack;
	}

	public InvocationContext getInvocationContext() {
		return invocationContext;
	}

}
