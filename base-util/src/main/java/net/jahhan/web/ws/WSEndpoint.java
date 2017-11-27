//package net.jahhan.web.ws;
//
//import java.io.IOException;
//import java.sql.Connection;
//import java.util.List;
//import java.util.Map;
//
//import javax.inject.Inject;
//import javax.servlet.http.HttpSession;
//import javax.websocket.CloseReason;
//import javax.websocket.CloseReason.CloseCodes;
//import javax.websocket.EndpointConfig;
//import javax.websocket.OnClose;
//import javax.websocket.OnMessage;
//import javax.websocket.OnOpen;
//import javax.websocket.Session;
//import javax.websocket.server.PathParam;
//import javax.websocket.server.ServerEndpoint;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import net.jahhan.cache.UserEntityCache;
//import net.jahhan.constant.SysConfiguration;
//import net.jahhan.context.BaseContext;
//import net.jahhan.context.InvocationContext;
//import net.jahhan.db.conn.DBConnFactory;
//import net.jahhan.exception.FrameworkException;
//import net.jahhan.web.UserEntity;
//import net.jahhan.web.UserSessionHelp;
//import net.jahhan.web.servlet.decodehandler.WSDecodeDataHandler;
//
//@ServerEndpoint(value = "/websocket/{path}", configurator = WebSocketConfigurator.class)
//public class WSEndpoint {
//
//	private Session session;
//	private HttpSession httpSession;
//	private Map<String, List<String>> headerMap;
//	private WSRequest wsRequest = new WSRequest();
//	private final static Logger logger = LoggerFactory.getLogger("websocket.servlet");
//	@Inject
//	private WSDecodeDataHandler decodeDataHandler;
//	@Inject
//	private WSSessionManager wsSessionManager;
//
//	@SuppressWarnings("unchecked")
//	@OnOpen
//	public void open(Session session, EndpointConfig config, @PathParam("path") String path) {
//		logger.info("wssession连接：" + path);
//		try {
//			this.session = session;
//			this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
//			this.headerMap = (Map<String, List<String>>) config.getUserProperties().get("headers");
//			if (validate() && SysConfiguration.getWsPathAccept().contains(path)) {
//				UserEntity userEntity = UserSessionHelp.getUserEntity(httpSession, UserEntity.class);
//				UserEntityCache.getInstance().setUserEntity(session.getId(), userEntity);
//				BaseContext applicationContext = BaseContext.CTX;
//				applicationContext.getUserSessionManager().wslogin(userEntity.getLoginRoleType(),
//						userEntity.getUserId(), session.getId(), path);
//				wsRequest.setWsSessionId(session.getId());
//				wsRequest.setHeaderMap(headerMap);
//				wsRequest.setHttpSession(httpSession);
//				wsRequest.setHttpSessionId(httpSession.getId());
//				wsRequest.setSession(session);
//				InvocationContext invocationContext = new InvocationContext(wsRequest);
//				applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);
//				applicationContext.setSessionId(httpSession.getId());
//				wsSessionManager.setSession(userEntity, session, path);
//				logger.info("*** WebSocket opened from sessionId " + httpSession.getId());
//			} else {
//				try {
//					session.close();
//				} catch (IOException e) {
//					logger.error("wssession关闭失败", e);
//				}
//			}
//		} catch (FrameworkException e) {
//			try {
//				CloseReason reason = new CloseReason(CloseCodes.RESERVED, e.getMessage());
//				session.close(reason);
//			} catch (IOException e1) {
//				logger.error("wssession关闭失败", e1);
//			}
//		}
//	}
//
//	@OnMessage
//	public void inMessage(String message) {
//		wsSessionManager.sendMessageByPath("chart", message);
//		BaseContext applicationContext = BaseContext.CTX;
//		InvocationContext invocationContext = new InvocationContext(wsRequest);
//		try {
//			applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);
//			decodeDataHandler.execute();
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		} catch (Error e) {
//			logger.error(e.getMessage(), e);
//		} finally {
//			Connection[] conns = invocationContext.getConnections().toArray(new Connection[0]);
//			if (conns != null) {
//				for (Connection conn : conns) {
//					logger.error("存在未关闭的连接,尝试全局连接...");
//					try {
//						DBConnFactory.freeConnection(conn);
//					} catch (Exception e) {
//						logger.error("尝试全局关闭连接失败。" + e.getMessage(), e);
//					}
//				}
//			}
//		}
//	}
//
//	@OnClose
//	public void end(@PathParam("path") String path) {
//		BaseContext applicationContext = BaseContext.CTX;
//		UserEntity userEntity = UserSessionHelp.getUserEntity(httpSession, UserEntity.class);
//		UserEntityCache.getInstance().removeUserEntity(session.getId());
//		applicationContext.getUserSessionManager().wslogout(userEntity.getLoginRoleType(), userEntity.getUserId(),
//				session.getId(), path);
//		wsSessionManager.remove(userEntity, this.session.getId(), path);
//		logger.info("*** WebSocket closed from sessionId " + httpSession.getId());
//	}
//
//	private boolean validate() {
//		if (null == httpSession) {
//			return false;
//		}
//		UserEntity userEntity = UserSessionHelp.getUserEntity(httpSession, UserEntity.class);
//		if (null == userEntity) {
//			return false;
//		}
//		return true;
//	}
//}