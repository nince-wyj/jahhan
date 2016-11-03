package net.jahhan.factory.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import net.jahhan.api.HttpCallBack;
import net.jahhan.cache.PipelineUrlQueue;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.LoggerFactory;

/**
 * @author nince
 */
public class AsyncHttpConnection implements HttpConnection {

	private final Logger logger = LoggerFactory.getInstance().getLogger(AsyncHttpConnection.class);
	private CloseableHttpAsyncClient httpAsyncClient;

	public AsyncHttpConnection() {
		httpAsyncClient = HttpAsyncClients.createDefault();
		httpAsyncClient.start();
	}

	@Override
	public HttpResponseEntity executeGet(String url, CookieStore cookie) {
		HttpResponseEntity httpResponseEntity = null;
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpGet request = new HttpGet(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		try {
			Future<HttpResponse> future = httpAsyncClient.execute(request, localContext, null);
			HttpResponse response = future.get();
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception e) {
			logger.error("异步executeGet 出现异常", e);
		} finally {
			request.abort();
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executeGet(String url, CookieStore cookie, Map<String, String> headers) {
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);
		HttpGet request = new HttpGet(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			request.addHeader(entry.getKey(), entry.getValue());
		}
		HttpResponseEntity httpResponseEntity = null;
		try {
			Future<HttpResponse> future = httpAsyncClient.execute(request, localContext, null);
			HttpResponse response = future.get();
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception e) {
			logger.error("异步executeGet 出现异常", e);
		} finally {
			request.abort();
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executeGetFile(String url, CookieStore cookie) {
		HttpResponseEntity httpResponseEntity = null;
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpGet request = new HttpGet(url);
		request.setHeader("Connection", "keep-alive");
		request.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.79 Safari/537.1");
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		InputStream inputStream = null;
		try {
			Future<HttpResponse> future = httpAsyncClient.execute(request, localContext, null);
			HttpResponse response = future.get();
			HttpEntity httpEntity = response.getEntity();
			int index = url.lastIndexOf("/");
			String fileName = url.substring(index + 1);
			inputStream = httpEntity.getContent();
			int len = -1;
			byte[] buf = new byte[(int) httpEntity.getContentLength()];
			int count = 0;
			while ((len = inputStream.read()) != -1) {
				buf[count] = (byte) len;
				count++;
			}
			httpResponseEntity = new HttpResponseEntity(buf, fileName, cookie,
					response.getStatusLine().getStatusCode());
		} catch (Exception e) {
			logger.error("异步executeGetFile 出现异常", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ex) {
					logger.error("异步executeGetFile 出现异常", ex);
				}
			}
			request.abort();
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executePost(String url, CookieStore cookie) {
		HttpResponseEntity httpResponseEntity = null;
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpPost request = new HttpPost(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		try {
			Future<HttpResponse> future = httpAsyncClient.execute(request, localContext, null);
			HttpResponse response = future.get();
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception e) {
			logger.error("异步executePost 出现异常", e);
		} finally {
			request.abort();
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executePost(String url, String parameter, CookieStore cookie) {
		HttpResponseEntity httpResponseEntity = null;
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpPost request = new HttpPost(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		StringEntity entity;
		try {
			entity = new StringEntity(parameter, Consts.UTF_8);
			request.setEntity(entity);
			Future<HttpResponse> future = httpAsyncClient.execute(request, localContext, null);
			HttpResponse response = future.get();
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception e) {
			logger.error("异步executePost 出现异常", e);
		} finally {
			request.abort();
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executePost(String url, Map<String, String> parameters, CookieStore cookie) {
		HttpResponseEntity httpResponseEntity = null;
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpPost request = new HttpPost(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		StringEntity entity;
		try {
			entity = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
			request.setEntity(entity);
			Future<HttpResponse> future = httpAsyncClient.execute(request, localContext, null);
			HttpResponse response = future.get();
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception e) {
			logger.error("异步executePost 出现异常", e);
		} finally {
			request.abort();
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executePost(String url, Map<String, String> parameters, CookieStore cookie,
			Map<String, String> headers) {
		HttpResponseEntity httpResponseEntity = null;
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpPost request = new HttpPost(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			request.addHeader(entry.getKey(), entry.getValue());
		}
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		StringEntity entity;
		try {
			entity = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
			request.setEntity(entity);
			Future<HttpResponse> future = httpAsyncClient.execute(request, localContext, null);
			HttpResponse response = future.get();
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception e) {
			logger.error("异步executePost 出现异常", e);
		} finally {
			request.abort();
		}
		return httpResponseEntity;
	}

	@Override
	public void executeGet(String url, CookieStore cookie, HttpCallBack callBack) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		URL uRL = null;
		try {
			uRL = new URL(url);
		} catch (MalformedURLException e) {
			FrameworkException.throwException(SystemErrorCode.UNKOWN_ERROR, url + "无法解析的url", e);
		}
		if (SysConfiguration.getPipelineOriginList().contains(uRL.getAuthority())) {
			PipelineUrlQueue.getInstance().getPipelineUrlQueue(uRL.getAuthority())
					.add(new PipeLineCallbackHold(uRL.getPath(), callBack, invocationContext));
			return;
		}
		AsyncHttpCallBack asyncHttpCallBack = new AsyncHttpCallBack(callBack, invocationContext);
		invocationContext.addAsyncHttpCallBack(asyncHttpCallBack);

		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);
		HttpGet request = new HttpGet(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		httpAsyncClient.execute(request, localContext, asyncHttpCallBack);
		request.abort();
	}

	@Override
	public void executeGet(String url, CookieStore cookie, Map<String, String> headers, HttpCallBack callBack) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		URL uRL = null;
		try {
			uRL = new URL(url);
		} catch (MalformedURLException e) {
			FrameworkException.throwException(SystemErrorCode.UNKOWN_ERROR, url + "无法解析的url", e);
		}
		if (SysConfiguration.getPipelineOriginList().contains(uRL.getAuthority())) {
			PipelineUrlQueue.getInstance().getPipelineUrlQueue(uRL.getAuthority())
					.add(new PipeLineCallbackHold(uRL.getPath(), callBack, invocationContext));
			return;
		}
		AsyncHttpCallBack asyncHttpCallBack = new AsyncHttpCallBack(callBack, invocationContext);
		invocationContext.addAsyncHttpCallBack(asyncHttpCallBack);

		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);
		HttpGet request = new HttpGet(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			request.addHeader(entry.getKey(), entry.getValue());
		}
		httpAsyncClient.execute(request, localContext, asyncHttpCallBack);
		request.abort();
	}

	@Override
	public void executeGetFile(String url, CookieStore cookie, HttpCallBack callBack) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		URL uRL = null;
		try {
			uRL = new URL(url);
		} catch (MalformedURLException e) {
			FrameworkException.throwException(SystemErrorCode.UNKOWN_ERROR, url + "无法解析的url", e);
		}
		if (SysConfiguration.getPipelineOriginList().contains(uRL.getAuthority())) {
			PipelineUrlQueue.getInstance().getPipelineUrlQueue(uRL.getAuthority())
					.add(new PipeLineCallbackHold(uRL.getPath(), callBack, invocationContext));
			return;
		}
		AsyncHttpCallBack asyncHttpCallBack = new AsyncHttpCallBack(callBack, invocationContext);
		invocationContext.addAsyncHttpCallBack(asyncHttpCallBack);
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpGet request = new HttpGet(url);
		request.setHeader("Connection", "keep-alive");
		request.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.79 Safari/537.1");
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		httpAsyncClient.execute(request, localContext, asyncHttpCallBack);
		request.abort();
	}

	@Override
	public void executePost(String url, CookieStore cookie, HttpCallBack callBack) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		AsyncHttpCallBack asyncHttpCallBack = new AsyncHttpCallBack(callBack, invocationContext);
		invocationContext.addAsyncHttpCallBack(asyncHttpCallBack);

		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpPost request = new HttpPost(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		httpAsyncClient.execute(request, localContext, asyncHttpCallBack);
		request.abort();
	}

	@Override
	public void executePost(String url, String parameter, CookieStore cookie, HttpCallBack callBack) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		AsyncHttpCallBack asyncHttpCallBack = new AsyncHttpCallBack(callBack, invocationContext);
		invocationContext.addAsyncHttpCallBack(asyncHttpCallBack);

		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpPost request = new HttpPost(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		StringEntity entity = new StringEntity(parameter, Consts.UTF_8);
		request.setEntity(entity);
		httpAsyncClient.execute(request, localContext, asyncHttpCallBack);
		request.abort();
	}

	@Override
	public void executePost(String url, Map<String, String> parameters, CookieStore cookie, HttpCallBack callBack) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		AsyncHttpCallBack asyncHttpCallBack = new AsyncHttpCallBack(callBack, invocationContext);
		invocationContext.addAsyncHttpCallBack(asyncHttpCallBack);

		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpPost request = new HttpPost(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		StringEntity entity = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
		request.setEntity(entity);
		httpAsyncClient.execute(request, localContext, asyncHttpCallBack);
		request.abort();
	}

	@Override
	public void executePost(String url, Map<String, String> parameters, CookieStore cookie, Map<String, String> headers,
			HttpCallBack callBack) {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		AsyncHttpCallBack asyncHttpCallBack = new AsyncHttpCallBack(callBack, invocationContext);
		invocationContext.addAsyncHttpCallBack(asyncHttpCallBack);

		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);

		HttpPost request = new HttpPost(url);
		request.setHeader("Connection", "close");
		request.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			request.addHeader(entry.getKey(), entry.getValue());
		}
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		StringEntity entity = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
		request.setEntity(entity);
		httpAsyncClient.execute(request, localContext, asyncHttpCallBack);
		request.abort();
	}

}
