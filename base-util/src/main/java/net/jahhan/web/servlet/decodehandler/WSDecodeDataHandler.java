//package net.jahhan.web.servlet.decodehandler;
//
//import java.io.UnsupportedEncodingException;
//import java.util.concurrent.ExecutorService;
//
//import javax.inject.Singleton;
//import javax.servlet.http.HttpSession;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.alibaba.fastjson.JSONObject;
//import com.google.inject.Inject;
//import com.google.inject.Injector;
//
//import net.jahhan.api.RequestMessage;
//import net.jahhan.api.ResponseMessage;
//import net.jahhan.cache.DebugActClassCache;
//import net.jahhan.constant.SysConfiguration;
//import net.jahhan.constant.SystemErrorCode;
//import net.jahhan.constant.enumeration.CryptEnum;
//import net.jahhan.constant.enumeration.RequestMethodEnum;
//import net.jahhan.constant.enumeration.ThreadPoolEnum;
//import net.jahhan.context.BaseContext;
//import net.jahhan.context.InvocationContext;
//import net.jahhan.exception.FrameworkException;
//import net.jahhan.factory.ThreadPoolFactory;
//import net.jahhan.handler.WorkHandler;
//import net.jahhan.thread.ResponseConsumeThread;
//import net.jahhan.web.action.ServiceRegisterHelper;
//import net.jahhan.web.action.WriteHelper;
//import net.jahhan.web.ws.WSRequest;
//
///**
// * 
// * @author nince
// */
//@Singleton
//public class WSDecodeDataHandler implements DecodeHandler {
//	private final static Logger logger = LoggerFactory.getLogger("WSDecodeDataHandler");
//
//	@Inject
//	private Injector injector;
//	@Inject
//	private ThreadPoolFactory threadPoolFactory;
//	@Inject
//	private ServiceRegisterHelper serviceRegisterHelper;
//	@Inject
//	private WriteHelper writeHelper;
//
//	public void setConsumeTime(long startTime, String actName) {
//		long now = System.currentTimeMillis();
//		long ms = now - startTime;
//		ResponseConsumeThread t = injector.getInstance(ResponseConsumeThread.class);
//		t.setActName(actName);
//		t.setConsumeTime(ms);
//		ExecutorService executeService = threadPoolFactory.getExecuteService(ThreadPoolEnum.FIXED);
//		executeService.execute(t);
//	}
//
//	@Override
//	public void execute() {
//		long startTime = System.currentTimeMillis();
//		BaseContext applicationContext = BaseContext.CTX;
//		InvocationContext invocationContext = applicationContext.getInvocationContext();
//		try {
//			WSRequest request = invocationContext.getWsRequest();
//
//			String requestJson = request.getMessage();
//
//			try {
//				requestJson = java.net.URLDecoder.decode(requestJson, "utf-8");
//				logger.debug("请求：" + requestJson);
//			} catch (UnsupportedEncodingException e) {
//				FrameworkException.throwException(SystemErrorCode.CONTENT_FORMAT_ERROR, "报文格式不正确");
//			}
//			if (StringUtils.isEmpty(requestJson)) {
//				FrameworkException.throwException(SystemErrorCode.CONTENT_NULL_ERROR, "报文为空");
//			}
//			String actName = "";
//			JSONObject json = null;
//			try {
//				json = JSONObject.parseObject(requestJson);
//
//				RequestMessage requestMessage = new RequestMessage();
//				invocationContext.setRequestMessage(requestMessage);
//				invocationContext.setResponseMessage(new ResponseMessage());
//				actName = json.getString("service_id");
//				requestMessage.setServiceName(actName);
//				requestMessage.setSign(json.getString("sign"));
//				requestMessage.setContent(json.get("content"));
//
//				HttpSession session = request.getHttpSession();
//				if (null != session) {
//					invocationContext.setSessionId(session.getId());
//					requestMessage.setToken(session.getId());
//					logger.debug("sessionid：" + session.getId());
//				}
//				WorkHandler service = serviceRegisterHelper.getService(RequestMethodEnum.WS,
//						requestMessage.getServiceName());
//				if (service == null) {
//					if (SysConfiguration.getIsDebug()) {
//						ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//						String className = DebugActClassCache.getInstance()
//								.getClassName(requestMessage.getServiceName());
//						serviceRegisterHelper.registerService(classLoader, injector, className);
//						service = serviceRegisterHelper.getService(RequestMethodEnum.WS,
//								requestMessage.getServiceName());
//					}
//					if (service == null) {
//						FrameworkException.throwException(SystemErrorCode.NO_SERVICE_INTERFACE, "接口不存在");
//					}
//				}
//				service.execute();
//				if (invocationContext.getResponseMessage().getErrorCode() != SystemErrorCode.SUCCESS) {
//					logger.debug("ACT方法：" + actName + "失败返回，错误信息："
//							+ invocationContext.getResponseMessage().getMessageInfo());
//				}
//				if (SysConfiguration.getRecordTimeConsume())
//					setConsumeTime(startTime, actName);
//				logger.debug(requestMessage.getServiceName() + " act finish!!");
//			} catch (FrameworkException e) {
//				throw e;
//			} catch (Exception e) {
//				if (json != null) {
//					logger.error(json.toString());
//				}
//				logger.error("报文格式不正确ex：" + actName, e);
//				FrameworkException.throwException(SystemErrorCode.CONTENT_FORMAT_ERROR, "报文格式不正确");
//			} catch (Error e) {
//				logger.error("报文格式不正确 error" + actName, e);
//				FrameworkException.throwException(SystemErrorCode.CONTENT_FORMAT_ERROR, "报文格式不正确");
//			}
//		} catch (FrameworkException e) {
//			ResponseMessage responseMessage = invocationContext.getResponseMessage();
//			if (null == responseMessage)
//				responseMessage = new ResponseMessage();
//			responseMessage.setMessageInfo(e.getMessage());
//			responseMessage.setErrorCode(e.getCode());
//			writeHelper.toWriteJson(responseMessage, RequestMethodEnum.WS, CryptEnum.PLAIN, new String[] {});
//		}
//	}
//}
