package net.jahhan.web.servlet.decodehandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Injector;

import net.jahhan.api.RequestMessage;
import net.jahhan.api.ResponseMessage;
import net.jahhan.cache.AsyncActionCache;
import net.jahhan.cache.DebugActClassCache;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.constant.enumeration.RequestMethodEnum;
import net.jahhan.constant.enumeration.ThreadPoolEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.ThreadPoolFactory;
import net.jahhan.handler.WorkHandler;
import net.jahhan.thread.AsyncThread;
import net.jahhan.thread.ResponseConsumeThread;
import net.jahhan.web.action.ServiceRegisterHelper;
import net.jahhan.web.action.WriteHelper;
import net.jahhan.web.servlet.listener.AppAsyncListener;

public class SIUDecodeDataHandler implements DecodeHandler {
	private final static Logger logger = LoggerFactory.getLogger("SIUDecodeDataHandler");

	private String serviceName;
	private String verNo;

	@Inject
	private Injector injector;
	@Inject
	private ThreadPoolFactory threadPoolFactory;
	@Inject
	private ServiceRegisterHelper serviceRegisterHelper;
	@Inject
	private WriteHelper writeHelper;

	public SIUDecodeDataHandler() {
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setVerNo(String verNo) {
		this.verNo = verNo;
	}

	@Override
	public void execute() {
		long startTime = System.currentTimeMillis();
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		try {
			HttpServletRequest request = invocationContext.getRequest();
			Map<String, String[]> parameterMap = request.getParameterMap();
			Iterator<String> parameterKeyIt = parameterMap.keySet().iterator();
			Map<String, Object> parameterMapChange = new HashMap<>();
			while (parameterKeyIt.hasNext()) {
				String key = parameterKeyIt.next();
				if (parameterMap.get(key).length == 1) {
					parameterMapChange.put(key, parameterMap.get(key)[0]);
				} else if (parameterMap.get(key).length > 1) {
					parameterMapChange.put(key, JSONObject.toJSONString(parameterMap.get(key)));
				}
			}
			try {
				RequestMessage requestMessage = new RequestMessage();
				requestMessage.setModifySince(request.getHeader("If-Modified-Since"));
				requestMessage.setServiceName(serviceName);
				requestMessage.setAppType(500);
				requestMessage.setVerNo(verNo);
				requestMessage.setRequestMap(parameterMapChange);
				requestMessage.setContent(parameterMapChange);
				requestMessage.setLocalAddr(request.getLocalAddr());

				invocationContext.setRequestMessage(requestMessage);
				invocationContext.setResponseMessage(new ResponseMessage());
				HttpSession session = request.getSession(false);
				if (null != session) {
					invocationContext.setSessionId(session.getId());
					requestMessage.setToken(session.getId());
					logger.debug("sessionid：" + session.getId());
				}
				WorkHandler service = serviceRegisterHelper.getService(RequestMethodEnum.SIU,
						requestMessage.getServiceName());
				if (service == null) {
					if (SysConfiguration.getIsDebug()) {
						ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
						String className = DebugActClassCache.getInstance()
								.getClassName(requestMessage.getServiceName());
						serviceRegisterHelper.registerService(classLoader, injector, className);
						service = serviceRegisterHelper.getService(RequestMethodEnum.SIU,
								requestMessage.getServiceName());
					}
					if (service == null) {
						FrameworkException.throwException(SystemErrorCode.NO_SERVICE_INTERFACE, "接口不存在");
					}
				}
				logger.debug(requestMessage.getServiceName() + " act!!");
				if (AsyncActionCache.getInstance().contains(requestMessage.getServiceName())) {
					request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
					AsyncContext asyncCtx = request.startAsync();
					asyncCtx.addListener(new AppAsyncListener());
					asyncCtx.setTimeout(SysConfiguration.getAsyncTimeOut());

					ExecutorService executeService = (ExecutorService) request
							.getServletContext().getAttribute("executor");
					AsyncThread asyncThread = injector.getInstance(AsyncThread.class);
					asyncThread.setAsyncCtx(asyncCtx);
					asyncThread.setInvocationContext(invocationContext);
					asyncThread.setActName(requestMessage.getServiceName());
					asyncThread.setService(service);
					executeService.execute(asyncThread);
				} else {
					service.execute();
					if (invocationContext.getResponseMessage().getErrorCode() != SystemErrorCode.SUCCESS) {
						logger.error("POST方法：" + serviceName + "失败返回，错误信息："
								+ invocationContext.getResponseMessage().getMessageInfo());
					}
					if (SysConfiguration.getRecordTimeConsume())
						setConsumeTime(startTime, requestMessage.getServiceName());
				}
			} catch (FrameworkException e) {
				throw e;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				FrameworkException.throwException(SystemErrorCode.UNKOWN_ERROR, e.getMessage());
			}
		} catch (FrameworkException e) {
			ResponseMessage responseMessage = invocationContext.getResponseMessage();
			responseMessage.setMessageInfo(e.getMessage());
			responseMessage.setErrorCode(e.getCode());
			writeHelper.toWriteJson(responseMessage, RequestMethodEnum.JSON, CryptEnum.PLAIN, new String[] {});
		}
	}

	public void setConsumeTime(long startTime, String actName) {
		long now = System.currentTimeMillis();
		long ms = now - startTime;
		ResponseConsumeThread t = injector.getInstance(ResponseConsumeThread.class);
		t.setActName(actName);
		t.setConsumeTime(ms);
		ExecutorService executeService = threadPoolFactory.getExecuteService(ThreadPoolEnum.FIXED);
		executeService.execute(t);
	}
}
