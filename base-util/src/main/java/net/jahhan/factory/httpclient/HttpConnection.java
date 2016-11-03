package net.jahhan.factory.httpclient;

import java.util.Map;

import org.apache.http.client.CookieStore;

import net.jahhan.api.HttpCallBack;

public interface HttpConnection {
	public HttpResponseEntity executeGet(String url, CookieStore cookie);

	public void executeGet(String url, CookieStore cookie, HttpCallBack callBack);

	public HttpResponseEntity executeGet(String url, CookieStore cookie, Map<String, String> headers);

	public void executeGet(String url, CookieStore cookie, Map<String, String> headers, HttpCallBack callBack);

	public HttpResponseEntity executeGetFile(String url, CookieStore cookie);

	public void executeGetFile(String url, CookieStore cookie, HttpCallBack callBack);

	public HttpResponseEntity executePost(String url, CookieStore cookie);

	public void executePost(String url, CookieStore cookie, HttpCallBack callBack);

	public HttpResponseEntity executePost(String url, String parameter, CookieStore cookie);

	public void executePost(String url, String parameter, CookieStore cookie, HttpCallBack callBack);

	public HttpResponseEntity executePost(String url, Map<String, String> parameters, CookieStore cookie);

	public void executePost(String url, Map<String, String> parameters, CookieStore cookie, HttpCallBack callBack);

	public HttpResponseEntity executePost(String url, Map<String, String> parameters, CookieStore cookie,
			Map<String, String> headers);

	public void executePost(String url, Map<String, String> parameters, CookieStore cookie, Map<String, String> headers,
			HttpCallBack callBack);
}
