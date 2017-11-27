package net.jahhan.cache.demand.impl;

import javax.websocket.Session;

import com.google.inject.Inject;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.repository.common.HttpSessionRepository;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.SessionStrategyEnum;
import net.jahhan.context.BaseContext;
import net.jahhan.demand.HostDemand;
import net.jahhan.demand.UserSessionDemand;
import net.jahhan.exception.FrameworkException;
import net.jahhan.web.ws.WSSessionManager;

public class UserSessionDemandImpl extends HttpSessionRepository implements UserSessionDemand {
	@Inject
	private WSSessionManager wsSessionManager;

	@Override
	public void login(Long loginRoleType, Long userId, String sessionId) {
		String bigKey = loginRoleType + ":" + userId + "_httpsession";
		SessionStrategyEnum sessionStrategy = SysConfiguration.getSessionStrategy();
		switch (sessionStrategy) {
		case MULTI:
			set(bigKey, sessionId, "1");
			break;
		case REJECT: {
			Redis redis = RedisFactory.getMainRedis(getType(), null);
			Long rt = redis.hsetnx(bigKey, sessionId, "1");
			if (rt != 1l) {
				FrameworkException.throwException(SystemErrorCode.USER_EXIIT, "用户已登陆");
			}
			break;
		}
		case TICK: {
			Redis redis = RedisFactory.getMainRedis(getType(), null);
			String oldSession = redis.get(bigKey);
			redis.del(oldSession);
			redis.del(bigKey);
			Long rt = redis.hsetnx(bigKey, sessionId, "1");
			if (rt != 1l) {
				FrameworkException.throwException(SystemErrorCode.USER_EXIIT, "用户已登陆");
			}
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void logout(Long loginRoleType, Long userId, String sessionId) {
		String bigKey = loginRoleType + ":" + userId + "_httpsession";
		SessionStrategyEnum sessionStrategy = SysConfiguration.getSessionStrategy();
		Redis redis = RedisFactory.getMainRedis(getType(), null);
		switch (sessionStrategy) {
		case MULTI:
			del(bigKey, sessionId);
			redis.del(sessionId);
			break;
		case REJECT: {
			redis.del(bigKey);
			redis.del(sessionId);
			break;
		}
		case TICK: {
			redis.del(bigKey);
			redis.del(sessionId);
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void wslogin(Long loginRoleType, Long userId, String wsSessionId, String path) {
		String bigKey = loginRoleType + ":" + userId + "_wssession";
		HostDemand hostDemand = BaseContext.CTX.getHostManager();
		String applicationWeb, applicationName, serverName;
		if (null != hostDemand) {
			applicationWeb = hostDemand.getThisHost();
			applicationName = String.valueOf(hostDemand.getPost());
			serverName = hostDemand.getHostType();
		} else {
			applicationWeb = System.getProperty("application.web");
			applicationName = System.getProperty("application.name");
			serverName = System.getProperty("server.name");
		}

		String uid = serverName + "-" + applicationWeb + "-" + applicationName;
		Long rt = setnx(bigKey, path, uid + ":" + wsSessionId);
		if (rt != 1l) {
			String sessionMessage = get(bigKey, path);
			String sessionHost = sessionMessage.split(":")[0];
			Session wssession = wsSessionManager.getSession(wsSessionId);
			if (sessionHost.equals(uid) && null == wssession) {
				set(bigKey, path, uid + ":" + wsSessionId);
			} else {
				FrameworkException.throwException(SystemErrorCode.WSSESSION_EXIIT, "已经登陆");
			}
		}
	}

	@Override
	public void wslogout(Long loginRoleType, Long userId, String wsSessionId, String path) {
		String bigKey = loginRoleType + ":" + userId + "_wssession";
		String sessionId = get(bigKey, path);
		HostDemand hostDemand = BaseContext.CTX.getHostManager();
		String applicationWeb, applicationName, serverName;
		if (null != hostDemand) {
			applicationWeb = hostDemand.getThisHost();
			applicationName = String.valueOf(hostDemand.getPost());
			serverName = hostDemand.getHostType();
		} else {
			applicationWeb = System.getProperty("application.web");
			applicationName = System.getProperty("application.name");
			serverName = System.getProperty("server.name");
		}
		String uid = serverName + "-" + applicationWeb + "-" + applicationName;
		if (sessionId.equals(uid + ":" + wsSessionId))
			del(bigKey, path);
	}

}
