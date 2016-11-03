package net.jahhan.factory.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.annotation.DbConn;
import net.jahhan.api.HttpCallBack;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.dbconnexecutor.DBConnExecutorFactory;
import net.jahhan.exception.BussinessException;
import net.jahhan.exception.FrameworkException;

public class PipelineHttpCallBack implements FutureCallback<List<HttpResponse>> {
	private static Logger logger = LoggerFactory.getLogger(PipelineHttpCallBack.class);
	private List<PipeLineCallbackHold> pipeLineCallbackHoldList;

	public PipelineHttpCallBack(List<PipeLineCallbackHold> pipeLineCallbackHoldList) {
		this.pipeLineCallbackHoldList = pipeLineCallbackHoldList;
	}

	@Override
	public void completed(List<HttpResponse> responseList) {
		for (int i = 0, length = responseList.size(); i < length; i++) {
			PipeLineCallbackHold pipeLineCallbackHold = pipeLineCallbackHoldList.get(i);
			HttpCallBack callBack = pipeLineCallbackHold.getHttpCallBack();
			DbConn transaction = callBack.getClass().getAnnotation(DbConn.class);
			ApplicationContext applicationContext = ApplicationContext.CTX;
			InvocationContext thisInvocationContext = new InvocationContext(pipeLineCallbackHold.getInvocationContext());
			applicationContext.getThreadLocalUtil().openThreadLocal(thisInvocationContext);
			thisInvocationContext.setConnectionType(transaction.value());
			DBConnExecutorFactory connExec = new DBConnExecutorFactory(transaction.value());
			try {
				connExec.beginConnection();
				HttpResponse response = responseList.get(i);
				HttpEntity httpEntity = response.getEntity();
				String result = EntityUtils.toString(httpEntity, "UTF-8");
				HttpResponseEntity httpResponseEntity;
				if (null == result) {
					InputStream inputStream = null;
					try {
						inputStream = httpEntity.getContent();
						int len = -1;
						byte[] buf = new byte[(int) httpEntity.getContentLength()];
						int count = 0;
						while ((len = inputStream.read()) != -1) {
							buf[count] = (byte) len;
							count++;
						}
						httpResponseEntity = new HttpResponseEntity(buf, "file", null,
								response.getStatusLine().getStatusCode());
					} finally {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (IOException ex) {
								logger.error("异步executeGetFile 出现异常", ex);
							}
						}
					}
				} else {
					httpResponseEntity = new HttpResponseEntity(result, null);
				}
				callBack.completed(httpResponseEntity);
				connExec.endConnection();
			} catch (BussinessException e) {
				connExec.rollback();
			} catch (FrameworkException e) {
				logger.error("DBConnHandler SystemException {}", e);
				connExec.rollback();
				throw e;
			} catch (Exception e) {
				logger.error("DBConnHandler exception {}", e);
				connExec.rollback();
			} catch (Error e) {
				logger.error("DBConnHandler error {}", e);
				connExec.rollback();
			} finally {
				connExec.close();
			}
			synchronized (thisInvocationContext.getAsyncHttpCallBackList()) {
				if (thisInvocationContext.asyncHttpCallBackListSize() == 0 && null != thisInvocationContext.getAsyncCtx()) {
					thisInvocationContext.getAsyncCtx().complete();
				}
			}
		}
		
	}

	@Override
	public void failed(Exception ex) {
	}

	@Override
	public void cancelled() {
	}
}
