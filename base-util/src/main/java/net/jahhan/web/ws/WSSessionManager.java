package net.jahhan.web.ws;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.cache.UserEntityCache;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.context.BaseContext;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.crypto.ICrypto;
import net.jahhan.utils.Assert;
import net.jahhan.web.UserEntity;

@Singleton
public class WSSessionManager {
	protected static Logger logger = LoggerFactory.getLogger("ws.Response");

	private Map<String, Session> sessionMap = new ConcurrentHashMap<>();
	private Map<Long, Entity> userMap = new ConcurrentHashMap<>();

	public Session getSession(String sessionId) {
		return sessionMap.get(sessionId);
	}

	public Session getSession(Long loginRoleType, Long userId, String path) {
		Entity entry = userMap.get(userId);
		if (null != entry && entry.getKey().equals(loginRoleType)) {
			Map<String, String> wsPathMap = entry.getValue();
			Assert.notNull(wsPathMap, "用户不存在！", SystemErrorCode.INVALID_SESSION);
			String sessionId = wsPathMap.get(path);
			Assert.notNull(sessionId, "用户不存在！", SystemErrorCode.INVALID_SESSION);
			return sessionMap.get(sessionId);
		}
		return null;
	}

	public void setSession(UserEntity userEntity, Session session, String path) {
		Entity entity;
		synchronized (userMap) {
			entity = userMap.get(userEntity.getUserId());
			if (null == entity) {
				entity = new Entity();
				userMap.put(userEntity.getUserId(), entity);
			}
		}
		entity.setKey(userEntity.getLoginRoleId());
		synchronized (entity) {
			Map<String, String> pathMap = entity.getValue();
			if (null == pathMap.get(path)) {
				pathMap.put(path, session.getId());
			} else {
				FrameworkException.throwException(SystemErrorCode.WSSESSION_EXIIT, "已连接同类型websocket");
			}
			entity.setValue(pathMap);
		}
		sessionMap.put(session.getId(), session);
		logger.debug("set sessionMap:" + sessionMap);

	}

	public void remove(UserEntity userEntity, String sessionId, String path) {
		Entity entity = userMap.get(userEntity.getUserId());
		if (null != entity) {
			synchronized (entity) {
				Map<String, String> pathMap = entity.getValue();
				String pathSessionId = pathMap.get(path);
				if (pathSessionId.equals(sessionId)) {
					pathMap.remove(path);
				}
			}
			sessionMap.remove(sessionId);
			logger.debug("remove sessionMap:" + sessionMap);
		}
	}

	public void sendMessage(String sessionId, UserEntity userEntity, String message) {
		Session session = sessionMap.get(sessionId);
		try {
			BaseContext applicationContext = BaseContext.CTX;
			String encryptKey = userEntity.getToken();
			ICrypto icrypto = applicationContext.getCrypto(CryptEnum.AES);
			String secrityMessage = icrypto.encrypt(message, encryptKey);
			logger.info("ws:" + sessionId + "返回值：" + message);
			logger.info("ws:" + sessionId + "返回加密串：" + secrityMessage);
			session.getBasicRemote().sendText(secrityMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessageByPath(String path, String message) {
		Iterator<Long> userKeyIt = userMap.keySet().iterator();
		BaseContext applicationContext = BaseContext.CTX;
		logger.info("ws广播" + path + "返回值：" + message);
		while (userKeyIt.hasNext()) {
			Map<String, String> pathMap = userMap.get(userKeyIt.next()).getValue();
			if (pathMap.containsKey(path)) {
				try {
					Session session = sessionMap.get(pathMap.get(path));
					UserEntity userEntity = UserEntityCache.getInstance().getUserEntity(session.getId());
					String encryptKey = userEntity.getToken();
					ICrypto icrypto = applicationContext.getCrypto(CryptEnum.AES);
					String secrityMessage = icrypto.encrypt(message, encryptKey);
					session.getBasicRemote().sendText(secrityMessage);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class Entity {
		private Long key;
		private Map<String, String> value = new ConcurrentHashMap<>();

		public Long getKey() {
			return key;
		}

		public void setKey(Long key) {
			this.key = key;
		}

		public Map<String, String> getValue() {
			return value;
		}

		public void setValue(Map<String, String> value) {
			this.value = value;
		}
	}
}
