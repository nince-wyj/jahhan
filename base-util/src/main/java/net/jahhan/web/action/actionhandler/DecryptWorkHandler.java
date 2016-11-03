package net.jahhan.web.action.actionhandler;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import com.alibaba.fastjson.JSONObject;

import net.jahhan.cache.ThirdEncryptKeyCache;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.constant.enumeration.RequestMethodEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.factory.crypto.ICrypto;
import net.jahhan.utils.Assert;
import net.jahhan.version.CustomVersion;
import net.jahhan.web.action.ActionHandler;
import net.jahhan.web.action.annotation.ActionService;
import net.jahhan.web.action.annotation.HandlerAnnocation;

/**
 * 解密
 * 
 * @author nince
 */
@HandlerAnnocation(700)
public class DecryptWorkHandler extends ActionHandler {

	private final Logger logger = LoggerFactory.getInstance().getLogger(DecryptWorkHandler.class);

	private CryptEnum requestSecurityType;
	private RequestMethodEnum requestMethodEnum;

	public DecryptWorkHandler(ActionHandler actionHandler, ActionService actionService) {
		this.nextHandler = actionHandler;
		this.requestSecurityType = actionService.requestEncrypt();
		this.requestMethodEnum = actionService.requestMethod();
	}

	@Override
	public void execute() {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();

		if (requestMethodEnum == RequestMethodEnum.JSON || requestMethodEnum == RequestMethodEnum.WS) {
			String contentJson = "";
			String verNo = invocationContext.getRequestMessage().getVerNo();
			boolean useSession = CustomVersion.useSession(invocationContext.getRequestMessage().getAppType(), verNo);
			Object content = invocationContext.getRequestMessage().getContent();
			if (!useSession) {
				JSONObject businessJson = (JSONObject) content;
				contentJson = businessJson.toJSONString();
			}

			if (useSession) {

				String encryptKey = null;
				if (requestSecurityType != CryptEnum.AES && requestSecurityType != CryptEnum.PLAIN
						&& requestSecurityType != CryptEnum.LOGIN) {
					FrameworkException.throwException(SystemErrorCode.NO_SERVICE_INTERFACE, "接口配置错误");
				}
				if (requestMethodEnum != RequestMethodEnum.WS && requestSecurityType == CryptEnum.LOGIN) {
					contentJson = (String) content;
					String thirdName = invocationContext.getRequestMessage().getThirdName();
					if (null != thirdName) {
						encryptKey = ThirdEncryptKeyCache.getInstance().getEncryptKey(thirdName);
					}
					if (null == encryptKey)
						encryptKey = SysConfiguration.getEncryptkey();
				} else if (requestMethodEnum == RequestMethodEnum.WS || requestSecurityType == CryptEnum.AES) {
					contentJson = (String) content;
					String sessionId = applicationContext.getSessionId();
					Assert.notNull(sessionId, SystemErrorCode.INVALID_SESSION);
					encryptKey = applicationContext.getUserEntity().getToken();
				} else {
					JSONObject businessJson = (JSONObject) content;
					contentJson = businessJson.toJSONString();
				}
				ICrypto icrypto = applicationContext.getCrypto(this.requestSecurityType);

				if (requestMethodEnum == RequestMethodEnum.WS || (requestSecurityType == CryptEnum.AES
						|| requestSecurityType == CryptEnum.PLAIN || requestSecurityType == CryptEnum.LOGIN)) {
					contentJson = icrypto.decrypt(contentJson.replace(" ", "+"), encryptKey);
					invocationContext.getRequestMessage().setContent(JSONObject.parseObject(contentJson));
				} else {
					FrameworkException.throwException(SystemErrorCode.NO_SERVICE_INTERFACE, "接口配置错误");
				}
				Assert.notNull(contentJson, "解密失败", SystemErrorCode.DECRYPT_ERROR);
			}

			Map<String, Object> reqMap = invocationContext.getRequestMessage().getRequestMap();
			Entry<String, Object> tempEntry = null;
			for (Iterator<Entry<String, Object>> it = JSONObject.parseObject(contentJson).entrySet().iterator(); it
					.hasNext();) {
				tempEntry = it.next();
				Object value = tempEntry.getValue();
				reqMap.put(tempEntry.getKey(), value);
			}
			logger.debug("decrypt jsonData = {}", contentJson);
			invocationContext.getRequestMessage().setMsg(contentJson);
		}
		nextHandler.execute();
	}
}
