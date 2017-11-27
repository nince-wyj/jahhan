//package net.jahhan.web;
//
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//import java.util.TimeZone;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//
//import org.apache.commons.lang3.RandomStringUtils;
//
//import net.jahhan.api.ResponseMessage;
//import net.jahhan.constant.SystemErrorCode;
//import net.jahhan.context.BaseContext;
//import net.jahhan.context.InvocationContext;
//import net.jahhan.utils.Assert;
//import net.jahhan.utils.BeanTools;
//import net.jahhan.utils.PropertiesUtil;
//
//public class UserSessionHelp {
//	public static String setUserSession(UserEntity userEntity) {
//		return setUserSession(userEntity, true);
//	}
//
//	public static String setUserSession(UserEntity userEntity, boolean reset) {
//		BaseContext CTX = BaseContext.CTX;
//		InvocationContext invocationContext = CTX.getInvocationContext();
//		if (reset) {
//			userEntity.setAddr(CTX.getRequestMessage().getLocalAddr());
//			userEntity.setAppType(CTX.getRequestMessage().getAppType());
//			String random = RandomStringUtils.randomAlphanumeric(16);
//			userEntity.setToken(random);
//
//			ResponseMessage responseMessage = invocationContext.getResponseMessage();
//			Map<String, Object> resultMap = new HashMap<>();
//			resultMap.put("token", random);
//			responseMessage.setResponseMap(resultMap);
//			if (null != CTX.getAuthorityService()) {
//				CTX.getAuthorityService().setAuthorityMD5(userEntity);
//			}
//		}
//
//		HttpServletRequest request = invocationContext.getRequest();
//		Assert.notNull(request, SystemErrorCode.CODE_ERROR);
//		HttpSession session = request.getSession(true);
//		Field[] declaredFields = userEntity.getClass().getDeclaredFields();
//		for (Field field : declaredFields) {
//			try {
//				field.setAccessible(true);
//				session.setAttribute(field.getName(), field.get(userEntity));
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			}
//		}
//
//		if (reset) {
//			HttpServletResponse response = invocationContext.getResponse();
//			String cookie = response.getHeader("Set-Cookie");
//			if (null != cookie) {
//				String[] cookieSplit = cookie.split(";");
//				String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
//				SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
//				format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
//				String[] split = cookieSplit[0].split("=");
//				String cookieName = PropertiesUtil.get("sys_baseconf", "cookieName");
//				String cookiePath = PropertiesUtil.get("sys_baseconf", "cookiePath");
//				if (null != cookiePath) {
//					response.setHeader("Set-Cookie", cookieName + "=" + split[1] + "; Path=" + cookiePath + "; Expires="
//							+ format.format(new Date(System.currentTimeMillis() + 30 * 24 * 3600 * 1000L)));
//				} else {
//					response.setHeader("Set-Cookie",
//							cookieName + "=" + split[1] + "; Path=" + request.getContextPath() + "/; Expires="
//									+ format.format(new Date(System.currentTimeMillis() + 30 * 24 * 3600 * 1000L)));
//				}
//			}
//			CTX.getUserSessionManager().login(userEntity.getLoginRoleType(), userEntity.getUserId(), session.getId());
//		}
//		return session.getId();
//	}
//
//	public static void removeUserSession() {
//		BaseContext CTX = BaseContext.CTX;
//		InvocationContext invocationContext = CTX.getInvocationContext();
//		HttpServletRequest request = invocationContext.getRequest();
//		if (null != request) {
//			HttpSession session = request.getSession(false);
//			if (null != session) {
//				CTX.getUserSessionManager().logout((Long) session.getAttribute("loginRoleType"),
//						(Long) session.getAttribute("userId"), session.getId());
//				session.invalidate();
//			}
//		} else {
//			HttpSession session = invocationContext.getWsRequest().getHttpSession();
//			if (null != session) {
//				CTX.getUserSessionManager().logout((Long) session.getAttribute("loginRoleType"),
//						(Long) session.getAttribute("userId"), session.getId());
//				session.invalidate();
//			}
//			try {
//				invocationContext.getWsRequest().getSession().close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public static UserEntity getUserEntity() {
//		return getUserEntity(UserEntity.class);
//	}
//
//	public static <T extends UserEntity> T getUserEntity(Class<? extends UserEntity> clazz) {
//		BaseContext CTX = BaseContext.CTX;
//		InvocationContext invocationContext = CTX.getInvocationContext();
//		HttpServletRequest request = invocationContext.getRequest();
//		HttpSession session = null;
//		if (null != request) {
//			session = request.getSession();
//		} else {
//			session = invocationContext.getWsRequest().getHttpSession();
//		}
//		return getUserEntity(session, clazz);
//	}
//
//	@SuppressWarnings("unchecked")
//	public static <T extends UserEntity> T getUserEntity(HttpSession session, Class<? extends UserEntity> clazz) {
//		T newInstance = null;
//		try {
//			newInstance = (T) clazz.newInstance();
//			Field[] fields = clazz.getDeclaredFields();
//			for (Field field : fields) {
//				field.setAccessible(true);
//				String name = field.getName();
//				Object value = session.getAttribute(name);
//				if (value != null) {
//					try {
//						field.set(newInstance, BeanTools.convertType(value, value.getClass(), field.getType()));
//					} catch (Exception e) {
//					}
//				}
//			}
//		} catch (InstantiationException | IllegalAccessException e) {
//		}
//		return newInstance;
//	}
//}
