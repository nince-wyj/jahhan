package net.jahhan.factory.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import net.jahhan.api.HttpCallBack;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.LoggerFactory;

/**
 * @author nince
 */
public class ThreadSafeSingtonHttpConnection implements HttpConnection {

	private final Logger logger = LoggerFactory.getInstance().getLogger(ThreadSafeSingtonHttpConnection.class);

	private CloseableHttpClient closeableHttpClient;

	public ThreadSafeSingtonHttpConnection() {
		int maxTotal = 300;
		int maxPerRout = 200;
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(maxTotal);
		cm.setDefaultMaxPerRoute(maxPerRout);
		ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				return 5 * 1000;
				// // Honor 'keep-alive' header
				// HeaderElementIterator it = new BasicHeaderElementIterator(
				// response.headerIterator(HTTP.CONN_KEEP_ALIVE));
				// while (it.hasNext()) {
				// HeaderElement he = it.nextElement();
				// String param = he.getName();
				// String value = he.getValue();
				// if (value != null && param.equalsIgnoreCase("timeout")) {
				// try {
				// return Long.parseLong(value) * 1000;
				// } catch (NumberFormatException ignore) {
				// }
				// }
				// }
				// HttpHost target = (HttpHost)
				// context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
				// if
				// ("www.naughty-server.com".equalsIgnoreCase(target.getHostName()))
				// {
				// // Keep alive for 5 seconds only
				// return 5 * 1000;
				// } else {
				// // otherwise keep alive for 30 seconds
				// return 30 * 1000;
				// }
			}

		};
		closeableHttpClient = HttpClients.custom().setConnectionManager(cm).setKeepAliveStrategy(myStrategy).build();
	}

	@Override
	public HttpResponseEntity executeGet(String url, CookieStore cookie) {
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Connection", "close");
		httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		HttpResponseEntity httpResponseEntity = null;
		try {
			CloseableHttpResponse response = closeableHttpClient.execute(httpGet, localContext);
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception ex) {
			logger.error("executeGet 出现异常", ex);
		} finally {
			httpGet.abort();
			closeableHttpClient = null;
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executeGet(String url, CookieStore cookie, Map<String, String> headers) {
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Connection", "close");
		httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpGet.addHeader(entry.getKey(), entry.getValue());
		}
		HttpResponseEntity httpResponseEntity = null;
		try {
			CloseableHttpResponse response = closeableHttpClient.execute(httpGet, localContext);
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception ex) {
			logger.error("executeGet 出现异常", ex);
		} finally {
			httpGet.abort();
			closeableHttpClient = null;
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executeGetFile(String url, CookieStore cookie) {
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Connection", "keep-alive");
		httpGet.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.79 Safari/537.1");
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		InputStream inputStream = null;
		HttpResponseEntity httpResponseEntity = null;
		try {
			HttpResponse response = closeableHttpClient.execute(httpGet);
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
		} catch (Exception ex) {
			logger.error("executeGetFile 出现异常", ex);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ex) {
					logger.error("executeGetFile 出现异常", ex);
				}
			}
			httpGet.abort();
			closeableHttpClient = null;
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executePost(String url, CookieStore cookie) {
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Connection", "close");
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		HttpResponseEntity httpResponseEntity = null;
		try {
			CloseableHttpResponse response = closeableHttpClient.execute(httpPost, localContext);
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception ex) {
			logger.error("executePost 出现异常", ex);
		} finally {
			httpPost.abort();
			closeableHttpClient = null;
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executePost(String url, String parameter, CookieStore cookie) {
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Connection", "close");
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		HttpResponseEntity httpResponseEntity = null;
		StringEntity entity;
		try {
			entity = new StringEntity(parameter, Consts.UTF_8);
			httpPost.setEntity(entity);
			CloseableHttpResponse response = closeableHttpClient.execute(httpPost, localContext);
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception ex) {
			logger.error("executePost 出现异常", ex);
		} finally {
			httpPost.abort();
			closeableHttpClient = null;
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executePost(String url, Map<String, String> parameters, CookieStore cookie) {
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Connection", "close");
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		HttpResponseEntity httpResponseEntity = null;
		StringEntity entity;
		try {
			entity = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
			httpPost.setEntity(entity);
			CloseableHttpResponse response = closeableHttpClient.execute(httpPost, localContext);
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception ex) {
			logger.error("executePost 出现异常", ex);
		} finally {
			httpPost.abort();
			closeableHttpClient = null;
		}
		return httpResponseEntity;
	}

	@Override
	public HttpResponseEntity executePost(String url, Map<String, String> parameters, CookieStore cookie,
			Map<String, String> headers) {
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setCookieStore(cookie);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader("Connection", "close");
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpPost.addHeader(entry.getKey(), entry.getValue());
		}
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		HttpResponseEntity httpResponseEntity = null;
		StringEntity entity;
		try {
			entity = new UrlEncodedFormEntity(nvps, Consts.UTF_8);
			httpPost.setEntity(entity);
			CloseableHttpResponse response = closeableHttpClient.execute(httpPost, localContext);
			HttpEntity httpEntity = response.getEntity();
			String result = EntityUtils.toString(httpEntity, "UTF-8");
			httpResponseEntity = new HttpResponseEntity(result, cookie);
		} catch (Exception ex) {
			logger.error("executePost 出现异常", ex);
		} finally {
			httpPost.abort();
			closeableHttpClient = null;
		}
		return httpResponseEntity;
	}

	@Override
	public void executeGet(String url, CookieStore cookie, HttpCallBack callBack) {
		FrameworkException.throwException(SystemErrorCode.UNSUPPORT_ERROR, "同步线程不支持异步http！");
	}

	@Override
	public void executeGet(String url, CookieStore cookie, Map<String, String> headers, HttpCallBack callBack) {
		FrameworkException.throwException(SystemErrorCode.UNSUPPORT_ERROR, "同步线程不支持异步http！");
	}

	@Override
	public void executeGetFile(String url, CookieStore cookie, HttpCallBack callBack) {
		FrameworkException.throwException(SystemErrorCode.UNSUPPORT_ERROR, "同步线程不支持异步http！");
	}

	@Override
	public void executePost(String url, CookieStore cookie, HttpCallBack callBack) {
		FrameworkException.throwException(SystemErrorCode.UNSUPPORT_ERROR, "同步线程不支持异步http！");
	}

	@Override
	public void executePost(String url, String parameter, CookieStore cookie, HttpCallBack callBack) {
		FrameworkException.throwException(SystemErrorCode.UNSUPPORT_ERROR, "同步线程不支持异步http！");
	}

	@Override
	public void executePost(String url, Map<String, String> parameters, CookieStore cookie, HttpCallBack callBack) {
		FrameworkException.throwException(SystemErrorCode.UNSUPPORT_ERROR, "同步线程不支持异步http！");
	}

	@Override
	public void executePost(String url, Map<String, String> parameters, CookieStore cookie, Map<String, String> headers,
			HttpCallBack callBack) {
		FrameworkException.throwException(SystemErrorCode.UNSUPPORT_ERROR, "同步线程不支持异步http！");
	}

}
