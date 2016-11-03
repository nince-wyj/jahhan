package net.jahhan.context;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Injector;

import net.jahhan.api.RequestMessage;
import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.constant.enumeration.FieldTypeEnum;
import net.jahhan.demand.ConsumeDemand;
import net.jahhan.demand.HostDemand;
import net.jahhan.demand.UserSessionDemand;
import net.jahhan.factory.CryptoUtilFactory;
import net.jahhan.factory.HttpConnectionUtilFactory;
import net.jahhan.factory.crypto.ICrypto;
import net.jahhan.guiceutils.ThreadLocalUtil;
import net.jahhan.handler.SerializerHandler;
import net.jahhan.service.AuthorityService;
import net.jahhan.web.UserEntity;
import net.jahhan.web.UserSessionHelp;
import net.jahhan.web.action.FieldHelper;
import net.jahhan.web.action.FieldTypeHandler;
import net.jahhan.web.action.FieldTypeHelper;

/**
 * @author nince
 */
@Singleton
public class ApplicationContext {

	public ApplicationContext() {
		if (null == CTX) {
			CTX = this;
		}
	}

	public static ApplicationContext CTX;
	@Inject
	private Injector injector;
	@Inject
	private ThreadLocalUtil<InvocationContext> threadLocalUtil;
	@Inject
	private CryptoUtilFactory cryptoFactory;
	@Inject
	private FieldTypeHelper fieldTypeHelper;
	@Inject
	private HttpConnectionUtilFactory httpConnectionFactory;
	@Inject
	private FieldHelper fieldHelper;
	@Inject
	@Named("hessian")
	private SerializerHandler serializer;

	public SerializerHandler getSerializer() {
		return serializer;
	}

	public Injector getInjector() {
		return injector;
	}

	public AuthorityService getAuthorityService() {
		try {
			return injector.getInstance(AuthorityService.class);
		} catch (Exception e) {
		}
		return null;
	}

	public UserSessionDemand getUserSessionManager() {
		try {
			return injector.getInstance(UserSessionDemand.class);
		} catch (Exception e) {
		}
		return null;
	}

	public ConsumeDemand getConsumeManager() {
		try {
			return injector.getInstance(ConsumeDemand.class);
		} catch (Exception e) {
		}
		return null;
	}

	public HostDemand getHostManager() {
		try {
			return injector.getInstance(HostDemand.class);
		} catch (Exception e) {
		}
		return null;
	}

	public ThreadLocalUtil<InvocationContext> getThreadLocalUtil() {
		return threadLocalUtil;
	}

	public InvocationContext getInvocationContext() {
		return threadLocalUtil.getValue();
	}

	public ICrypto getCrypto(CryptEnum cryptEnum) {
		return cryptoFactory.getCrypto(cryptEnum);
	}

	public FieldTypeHandler getFieldTypeHandler(FieldTypeEnum fieldTypeEnum) {
		return fieldTypeHelper.getFieldTypeHandler(fieldTypeEnum);
	}

	public HttpConnectionUtilFactory getHttpConnectionFactory() {
		return httpConnectionFactory;
	}

	public FieldHelper getFieldManager() {
		return fieldHelper;
	}

	public String getRealPath() {
		return threadLocalUtil.getValue().getRequest().getServletContext().getRealPath("/");
	}

	public void setSessionId(String sessionId) {
		threadLocalUtil.getValue().setSessionId(sessionId);
	}

	public String getSessionId() {
		return threadLocalUtil.getValue().getSessionId();
	}

	public RequestMessage getRequestMessage() {
		return threadLocalUtil.getValue().getRequestMessage();
	}

	public String setUserSession(UserEntity userEntity) {
		return UserSessionHelp.setUserSession(userEntity);
	}

	public String setUserSession(UserEntity userEntity, boolean reset) {
		return UserSessionHelp.setUserSession(userEntity, reset);
	}

	public void removeUserSession() {
		UserSessionHelp.removeUserSession();
	}

	public UserEntity getUserEntity() {
		return UserSessionHelp.getUserEntity();
	}

	public <T extends UserEntity> T getUserEntity(Class<? extends UserEntity> clazz) {
		return UserSessionHelp.getUserEntity(clazz);
	}

	public boolean isWriteConnection() {
		InvocationContext invocationContext = threadLocalUtil.getValue();
		return null != invocationContext && (invocationContext.getConnectionType().equals(DBConnectionType.WEAK_WRITE)
				|| invocationContext.getConnectionType().equals(DBConnectionType.BATCH_WRITE)
				|| invocationContext.getConnectionType().equals(DBConnectionType.DISTRIBUTED_WEAK_WRITE));
	}
}
