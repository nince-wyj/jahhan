//package net.jahhan.web.servlet;
//
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ExecutorService;
//
//import javax.inject.Singleton;
//import javax.servlet.AsyncContext;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.lang3.BooleanUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.google.inject.Inject;
//import com.google.inject.Injector;
//
//import net.jahhan.api.ResponseMessage;
//import net.jahhan.cache.AsyncActionCache;
//import net.jahhan.constant.SysConfiguration;
//import net.jahhan.constant.SystemErrorCode;
//import net.jahhan.constant.enumeration.RequestMethodEnum;
//import net.jahhan.context.BaseContext;
//import net.jahhan.context.InvocationContext;
//import net.jahhan.exception.FrameworkException;
//import net.jahhan.handler.WorkHandler;
//import net.jahhan.thread.AsyncThread;
//import net.jahhan.utils.Assert;
//import net.jahhan.utils.PropertiesUtil;
//import net.jahhan.web.action.ServiceRegisterHelper;
//import net.jahhan.web.context.HeaderConstant;
//import net.jahhan.web.context.WebRequestMessage;
//import net.jahhan.web.servlet.listener.AppAsyncListener;
//
///**
// * @author nince
// */
//@Singleton
////@WebServlet(name = "actionServlet", urlPatterns = { "/action" })
//public class ActionServlet extends HttpServlet {
//
//	private static final long serialVersionUID = -6896204388024224735L;
//	private final static Logger logger = LoggerFactory.getLogger("actionServlet.servlet");
//	@Inject
//	private Injector injector;
//	@Inject
//	private ServiceRegisterHelper serviceRegisterHelper;
//
//	@Override
//	protected void service(HttpServletRequest req, HttpServletResponse resp) {
//		resp.setContentType("application/json;charset=utf-8");
//		resp.setHeader("Access-Control-Allow-Credentials", "true");
//		resp.addHeader("P3P", "CP=CAO PSA OUR");
//		if (BooleanUtils.toBoolean(PropertiesUtil.get("web", "html.allowAllOrigin"))) {
//			resp.addHeader("Access-Control-Allow-Origin", "*");
//			resp.addHeader("P3P",
//					"CP=\"CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR\"");
//			// resp.addHeader("Access-Control-Allow-Headers", "Origin,
//			// X-Requested-With, Content-Type, Accept");
//		} else {
//			resp.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
//		}
//
//		BaseContext applicationContext = BaseContext.CTX;
//		InvocationContext invocationContext = new InvocationContext(req, resp);
//		applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);
//		loadService(req, resp);
//	}
//
//	private void loadService(HttpServletRequest request, HttpServletResponse response) {
//		try {
//			BaseContext applicationContext = BaseContext.CTX;
//			InvocationContext invocationContext = applicationContext.getInvocationContext();
//			WebRequestMessage requestMessage = new WebRequestMessage();
//			invocationContext.setRequestMessage(requestMessage);
//			invocationContext.setResponseMessage(new ResponseMessage());
//			String requestURI = request.getRequestURI();
//			int index = requestURI.indexOf("/action/");
//			String serviceName = requestURI.substring(index+7);
//			logger.debug("收到请求服务：" + serviceName);
//			Assert.notNullString(serviceName, "未知url", SystemErrorCode.NO_SERVICE_INTERFACE);
//			requestMessage.setServiceName(serviceName);
//			Field[] fields = HeaderConstant.class.getFields();
//			Map<String, String> headMap = new HashMap<>();
//			for (Field field : fields) {
//				String headKey = (String) field.get(null);
//				String headValue = request.getHeader(headKey);
//				if(null!=headValue){
//					headMap.put(headKey, headValue);
//				}
//			}
//			requestMessage.setHeadMessage(headMap);
//			WorkHandler service = serviceRegisterHelper.getService(RequestMethodEnum.JSON,
//					requestMessage.getServiceName());
//			if (AsyncActionCache.getInstance().contains(requestMessage.getServiceName())) {
//				request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
//				AsyncContext asyncCtx = request.startAsync();
//				asyncCtx.addListener(new AppAsyncListener());
//				asyncCtx.setTimeout(SysConfiguration.getAsyncTimeOut());
//
//				ExecutorService executeService = (ExecutorService) request.getServletContext()
//						.getAttribute("executor");
//				AsyncThread asyncThread = injector.getInstance(AsyncThread.class);
//				asyncThread.setAsyncCtx(asyncCtx);
//				asyncThread.setInvocationContext(invocationContext);
//				asyncThread.setActName(serviceName);
//				asyncThread.setService(service);
//				executeService.execute(asyncThread);
//			} else {
//				service.execute();
//				if (invocationContext.getResponseMessage().getErrorCode() != SystemErrorCode.SUCCESS) {
//					logger.debug("ACT方法：" + serviceName + "失败返回，错误信息："
//							+ invocationContext.getResponseMessage().getMessageInfo());
//				}
//				logger.debug(requestMessage.getServiceName() + " act finish!!");
//			}
//		} catch (IllegalArgumentException | IllegalAccessException e) {
//			logger.error(e.getMessage());
//			try {
//				response.sendError(500, e.getMessage());
//			} catch (IOException e1) {
//				logger.error(e1.getMessage());
//			}
//		}catch (FrameworkException e) {
//			if(e.getCode()<600){
//				
//			}
//		}
//	}
//}
