//package net.jahhan.web.action.actionhandler;
//
//import org.slf4j.Logger;
//
//import net.jahhan.api.RequestMessage;
//import net.jahhan.constant.SystemErrorCode;
//import net.jahhan.constant.enumeration.CryptEnum;
//import net.jahhan.constant.enumeration.RequestMethodEnum;
//import net.jahhan.context.BaseContext;
//import net.jahhan.context.InvocationContext;
//import net.jahhan.exception.FrameworkException;
//import net.jahhan.factory.LoggerFactory;
//import net.jahhan.factory.crypto.ICrypto;
//import net.jahhan.version.CustomVersion;
//import net.jahhan.web.action.ActionHandler;
//import net.jahhan.web.action.annotation.ActionService;
//import net.jahhan.web.action.annotation.HandlerAnnocation;
//
///**
// * 签名效验
// * 
// * @author nince
// */
//@HandlerAnnocation(400)
//public class SignValidateWorkHandler extends ActionHandler {
//
//	private final Logger logger = LoggerFactory.getInstance().getLogger(SignValidateWorkHandler.class);
//
//	private CryptEnum requestEncrypt;
//	private final RequestMethodEnum requestMethod;
//
//	public SignValidateWorkHandler(ActionHandler actionHandler, ActionService actionService) {
//		this.nextHandler = actionHandler;
//		this.requestEncrypt = actionService.requestEncrypt();
//		this.requestMethod = actionService.requestMethod();
//	}
//
//	@Override
//	public void execute() {
//		if (!requestMethod.equals(RequestMethodEnum.BPMRECESSFILL)) {
//			BaseContext applicationContext = BaseContext.CTX;
//			InvocationContext invocationContext = applicationContext.getInvocationContext();
//			String verNo = invocationContext.getRequestMessage().getVerNo();
//			boolean useSession = CustomVersion.useSession(invocationContext.getRequestMessage().getAppType(), verNo);
//			if (useSession && requestEncrypt == CryptEnum.SIGN) {
//				RequestMessage requestMessage = invocationContext.getRequestMessage();
//
//				ICrypto md5EnCryption = applicationContext.getCrypto(CryptEnum.SIGN);
//				String md5Result = md5EnCryption.encrypt(requestMessage.getMsg(), null);
//
//				logger.debug("md5Result={}", md5Result);
//				logger.debug("sign={}", requestMessage.getSign());
//
//				if (requestMessage.getSign() == null) {
//					FrameworkException.throwException(SystemErrorCode.SIGN_ERROR, "签名验证失败");
//				}
//
//				if (!md5Result.toUpperCase().equals(requestMessage.getSign().toUpperCase())) {
//					FrameworkException.throwException(SystemErrorCode.SIGN_ERROR, "签名验证失败");
//				}
//			}
//		}
//		nextHandler.execute();
//
//	}
//}
