package net.jahhan.api;

import net.jahhan.factory.httpclient.HttpResponseEntity;

public interface HttpCallBack {
	void completed(HttpResponseEntity httpResponseEntity);

	void failed(Exception ex);

	void cancelled();
}