package net.jahhan.web.servlet.decodehandler;

import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Injector;

import net.jahhan.api.RequestMessage;
import net.jahhan.api.ResponseMessage;
import net.jahhan.cache.AsyncActionCache;
import net.jahhan.cache.DebugActClassCache;
import net.jahhan.constant.HeaderConstant;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.constant.enumeration.RequestMethodEnum;
import net.jahhan.constant.enumeration.ThreadPoolEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.HeadMessage;
import net.jahhan.context.InvocationContext;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.ThreadPoolFactory;
import net.jahhan.handler.WorkHandler;
import net.jahhan.thread.AsyncThread;
import net.jahhan.thread.ResponseConsumeThread;
import net.jahhan.version.CustomVersion;
import net.jahhan.web.action.ServiceRegisterHelper;
import net.jahhan.web.action.WriteHelper;
import net.jahhan.web.servlet.listener.AppAsyncListener;

/**
 * 适配老接口，对前端协议进行适配解码: 车位管家协议适配
 * 
 * @author nince
 */
@Singleton
public class SICDecodeDataHandler implements DecodeHandler {
	private final static Logger logger = LoggerFactory.getLogger("SICDecodeDataHandler");

	@Inject
	private Injector injector;
	@Inject
	private ThreadPoolFactory threadPoolFactory;
	@Inject
	private ServiceRegisterHelper serviceRegisterHelper;
	@Inject
	private WriteHelper writeHelper;

	public void setConsumeTime(long startTime, String actName) {
		long now = System.currentTimeMillis();
		long ms = now - startTime;
		ResponseConsumeThread t = injector.getInstance(ResponseConsumeThread.class);
		t.setActName(actName);
		t.setConsumeTime(ms);
		ExecutorService executeService = threadPoolFactory.getExecuteService(ThreadPoolEnum.FIXED);
		executeService.execute(t);
	}

	@Override
	public void execute() {
		long startTime = System.currentTimeMillis();
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		try {
			HttpServletRequest request = invocationContext.getRequest();

			String requestJson = request.getParameter("content");
			logger.debug("收到请求：" + requestJson);
			if (StringUtils.isEmpty(requestJson)) {
				FrameworkException.throwException(SystemErrorCode.CONTENT_NULL_ERROR, "报文为空");
			}
			String actName = "";
			JSONObject json = null;
			try {
				json = JSONObject.parseObject(requestJson);

				HeadMessage headMessage = new HeadMessage();
				headMessage.setLat(json.getString(HeaderConstant.lat));
				headMessage.setLng(json.getString(HeaderConstant.lon));
				headMessage.setCityId(json.getLongValue(HeaderConstant.cityId));
				headMessage.setCityName(json.getString(HeaderConstant.cityName));
				String ip = getIpAddr(request);
				headMessage.setIp(ip);

				RequestMessage requestMessage = new RequestMessage();
				invocationContext.setRequestMessage(requestMessage);
				invocationContext.setResponseMessage(new ResponseMessage());
				requestMessage.setHeadMessage(headMessage);
				actName = json.getString("service_id");
				requestMessage.setServiceName(actName);
				requestMessage.setAppType(json.getIntValue("app_type"));
				requestMessage
						.setThirdName(null == json.getString("company_code") ? null : json.getString("company_code"));
				String verNo = json.getString("ver_name");
				requestMessage.setVerNo(verNo);
				requestMessage.setSign(json.getString("sign"));
				requestMessage.setContent(json.get("content"));
				requestMessage.setLocalAddr(request.getLocalAddr());

				boolean useSession = CustomVersion.useSession(json.getIntValue("app_type"), verNo);
				if (useSession) {
					HttpSession session = request.getSession(false);
					if (null != session) {
						invocationContext.setSessionId(session.getId());
						requestMessage.setToken(session.getId());
						logger.debug("sessionid：" + session.getId());
					}
				}
				WorkHandler service = serviceRegisterHelper.getService(RequestMethodEnum.JSON,
						requestMessage.getServiceName());
				if (service == null) {
					if (SysConfiguration.getIsDebug()) {
						ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
						String className = DebugActClassCache.getInstance()
								.getClassName(requestMessage.getServiceName());
						serviceRegisterHelper.registerService(classLoader, injector, className);
						service = serviceRegisterHelper.getService(RequestMethodEnum.JSON,
								requestMessage.getServiceName());
					}
					if (service == null) {
						FrameworkException.throwException(SystemErrorCode.NO_SERVICE_INTERFACE, "接口不存在");
					}
				}
				if (AsyncActionCache.getInstance().contains(requestMessage.getServiceName())) {
					request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
					AsyncContext asyncCtx = request.startAsync();
					asyncCtx.addListener(new AppAsyncListener());
					asyncCtx.setTimeout(SysConfiguration.getAsyncTimeOut());

					ExecutorService executeService = (ExecutorService) request.getServletContext()
							.getAttribute("executor");
					AsyncThread asyncThread = injector.getInstance(AsyncThread.class);
					asyncThread.setAsyncCtx(asyncCtx);
					asyncThread.setInvocationContext(invocationContext);
					asyncThread.setActName(actName);
					asyncThread.setService(service);
					executeService.execute(asyncThread);
				} else {
					service.execute();
					if (invocationContext.getResponseMessage().getErrorCode() != SystemErrorCode.SUCCESS) {
						logger.debug("ACT方法：" + actName + "失败返回，错误信息："
								+ invocationContext.getResponseMessage().getMessageInfo());
					}
					if (SysConfiguration.getRecordTimeConsume())
						setConsumeTime(startTime, actName);
					logger.debug(requestMessage.getServiceName() + " act finish!!");
				}
			} catch (FrameworkException e) {
				throw e;
			} catch (Exception e) {
				if (json != null) {
					logger.error(json.toString());
				}
				logger.error("报文格式不正确ex：" + actName, e);
				FrameworkException.throwException(SystemErrorCode.CONTENT_FORMAT_ERROR, "报文格式不正确");
			} catch (Error e) {
				logger.error("报文格式不正确 error：" + actName, e);
				FrameworkException.throwException(SystemErrorCode.CONTENT_FORMAT_ERROR, "报文格式不正确");
			}
		} catch (FrameworkException e) {
			ResponseMessage responseMessage = invocationContext.getResponseMessage();
			if (null == responseMessage)
				responseMessage = new ResponseMessage();
			responseMessage.setMessageInfo(e.getMessage());
			responseMessage.setErrorCode(e.getCode());
			writeHelper.toWriteJson(responseMessage, RequestMethodEnum.JSON, CryptEnum.PLAIN, new String[] {});
		}
	}

	public String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		String[] ips = ip.split(",");
		for (int i = 0; i < ips.length; i++) {
			if (!ips[i].equalsIgnoreCase("unknown")) {
				ip = ips[i];
				break;
			}
		}
		return ip;
	}
}
