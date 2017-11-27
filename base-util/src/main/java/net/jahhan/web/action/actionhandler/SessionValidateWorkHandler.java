//package net.jahhan.web.action.actionhandler;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//
//import net.jahhan.constant.SystemErrorCode;
//import net.jahhan.constant.enumeration.LoginEnum;
//import net.jahhan.constant.enumeration.RequestMethodEnum;
//import net.jahhan.context.BaseContext;
//import net.jahhan.context.InvocationContext;
//import net.jahhan.exception.FrameworkException;
//import net.jahhan.factory.LoggerFactory;
//import net.jahhan.utils.PropertiesUtil;
//import net.jahhan.version.CustomVersion;
//import net.jahhan.web.UserEntity;
//import net.jahhan.web.UserSessionHelp;
//import net.jahhan.web.action.ActionHandler;
//import net.jahhan.web.action.annotation.ActionService;
//import net.jahhan.web.action.annotation.HandlerAnnocation;
//
///**
// * session处理,sessionid采用web容器自生成的
// * 
// * @author nince
// */
//@HandlerAnnocation(800)
//public class SessionValidateWorkHandler extends ActionHandler {
//	private final Logger logger = LoggerFactory.getInstance().getLogger(SessionValidateWorkHandler.class);
//	private final LoginEnum loginEnum;
//	private final RequestMethodEnum requestMethod;
//
//	public SessionValidateWorkHandler(ActionHandler actionHandler, ActionService actionService) {
//		this.nextHandler = actionHandler;
//		this.loginEnum = actionService.requireLogin();
//		this.requestMethod = actionService.requestMethod();
//	}
//
//	@Override
//	public void execute() {
//		if (!requestMethod.equals(RequestMethodEnum.BPMRECESSFILL)) {
//			BaseContext applicationContext = BaseContext.CTX;
//			InvocationContext invocationContext = applicationContext.getInvocationContext();
//			if (!requestMethod.equals(RequestMethodEnum.WS)) {
//				String verNo = invocationContext.getRequestMessage().getVerNo();
//				boolean useSession = CustomVersion.useSession(invocationContext.getRequestMessage().getAppType(),
//						verNo);
//				if (useSession && loginEnum == LoginEnum.YES) {
//					String sessionId = applicationContext.getSessionId();
//					if (StringUtils.isEmpty(sessionId)) {
//						FrameworkException.throwException(SystemErrorCode.SESSION_NO_EXIST, "session过期或无效");
//					}
//					if (!validate(sessionId)) {
//						logger.error("session 不合法：" + sessionId);
//						FrameworkException.throwException(SystemErrorCode.INVALID_SESSION, "session不合法");
//					}
//				}
//			}
//			HttpServletResponse response = invocationContext.getResponse();
//			HttpServletRequest request = invocationContext.getRequest();
//			Cookie[] cookies = request.getCookies();
//			if (null != cookies) {
//				String cookieName = PropertiesUtil.get("sys_baseconf", "cookieName");
//				for (int i = 0, length = cookies.length; i < length; i++) {
//					Cookie responseCookie = cookies[i];
//					if (responseCookie.getName().equals(cookieName)) {
//						responseCookie.setMaxAge(3600 * 24 * 30);
//						String cookiePath = PropertiesUtil.get("sys_baseconf", "cookiePath");
//						if (null != cookiePath) {
//							responseCookie.setPath(cookiePath);
//						} else {
//							responseCookie.setPath(request.getContextPath() + "/");
//						}
//						response.addCookie(responseCookie);
//						break;
//					}
//				}
//			}
//		}
//		nextHandler.execute();
//	}
//
//	/**
//	 * 1.验证生成sessionid时,网卡序列号是否一致.
//	 * 2.验证生成sessionid时,来源是否一致，是否在合法的来源列表中(需要在nginx层配链路层信息，所以先去除).
//	 * 
//	 * @param sessionid
//	 * @return
//	 */
//	private boolean validate(String sessionId) {
//		BaseContext applicationContext = BaseContext.CTX;
//		InvocationContext invocationContext = applicationContext.getInvocationContext();
//		HttpServletRequest request = invocationContext.getRequest();
//		HttpSession session = request.getSession(false);
//		if (null == session) {
//			return false;
//		}
//		UserEntity userEntity = UserSessionHelp.getUserEntity(session, UserEntity.class);
//		if (null == userEntity) {
//			return false;
//		}
//		if (!userEntity.getAddr().equals(applicationContext.getRequestMessage().getLocalAddr())) {
//			return false;
//		}
//		return true;
//	}
//}
