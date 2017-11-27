//package net.jahhan.web.action.handler;
//
//import javax.inject.Inject;
//
//import org.slf4j.Logger;
//
//import net.jahhan.api.ResponseMessage;
//import net.jahhan.cache.Redis;
//import net.jahhan.cache.RedisConstants;
//import net.jahhan.cache.RedisFactory;
//import net.jahhan.constant.SystemErrorCode;
//import net.jahhan.constant.enumeration.CryptEnum;
//import net.jahhan.constant.enumeration.FastBackEnum;
//import net.jahhan.constant.enumeration.ResponseTypeEnum;
//import net.jahhan.context.BaseContext;
//import net.jahhan.context.InvocationContext;
//import net.jahhan.factory.LoggerFactory;
//import net.jahhan.utils.Assert;
//import net.jahhan.web.UserEntity;
//import net.jahhan.web.action.ActionHandler;
//import net.jahhan.web.action.annotation.ActionService;
//import net.jahhan.web.action.annotation.HandlerAnnocation;
//
///**
// * @author nince
// */
//@HandlerAnnocation(900)
//public class FastBackHandler extends ActionHandler {
//
//	private final Logger logger = LoggerFactory.getInstance().getLogger(FastBackHandler.class);
//
//	private ResponseTypeEnum responseType;
//	private CryptEnum requestEncrypt;
//	private boolean fastBack;
//	private boolean fastBackFail;
//	private int blockTime;
//	private String act;
//	private FastBackEnum fastBackType;
//	@Inject
//	private net.jahhan.spi.SerializerHandler serializer;
//
//	public FastBackHandler(ActionHandler actionHandler, ActionService actionService) {
//		this.nextHandler = actionHandler;
//		this.requestEncrypt = actionService.requestEncrypt();
//		this.responseType = actionService.responseType();
//		this.fastBack = actionService.fastBack();
//		this.blockTime = actionService.blockTime();
//		this.act = actionService.act();
//		this.fastBackType = actionService.fastBackType();
//		this.fastBackFail = actionService.fastBackFail();
//	}
//
//	private static String PRE = "fast_back:";
//
//	@Override
//	public void execute() {
//		if (responseType.equals(ResponseTypeEnum.FILE) || !requestEncrypt.equals(CryptEnum.SIGN) || !fastBack
//				|| blockTime < 1) {
//			nextHandler.execute();
//		} else {
//			BaseContext applicationContext = BaseContext.CTX;
//			InvocationContext invocationContext = applicationContext.getInvocationContext();
//
//			Redis redis = RedisFactory.getMainRedis(RedisConstants.TABLE_COMMON, null);
//			String key = "";
//			switch (fastBackType) {
//			case SESSION:
//				Assert.notNull(applicationContext.getSessionId(), "session过期或无效", SystemErrorCode.INVALID_SESSION);
//				key = PRE + act + "_" + invocationContext.getRequestMessage().getSign() + "_"
//						+ applicationContext.getSessionId();
//				break;
//			case USERID:
//				UserEntity userEntity = invocationContext.getUserEntity();
//				Assert.notNull(userEntity, "session过期或无效", SystemErrorCode.INVALID_SESSION);
//				key = PRE + act + "_" + invocationContext.getRequestMessage().getSign() + "_" + userEntity.getUserId();
//				break;
//			case ALL:
//				key = PRE + act + "_" + invocationContext.getRequestMessage().getSign();
//				break;
//			default:
//				break;
//			}
//			byte[] json = redis.getBinary(key.getBytes());
//			if (json != null) {
//				if (fastBackFail) {
//					ResponseMessage responseMessage = invocationContext.getResponseMessage();
//					responseMessage.setErrorCode(SystemErrorCode.FAST_RESPONSE_ERROR);
//					responseMessage.setMessageInfo("快速返回失败");
//					responseMessage.setResponseMap(null);
//					return;
//				}
//				invocationContext.setResponseMessage((ResponseMessage) serializer.deserializeInto(json));
//				logger.debug("快速返回：" + act);
//				return;
//			}
//			nextHandler.execute();
//			ResponseMessage responseMessage = invocationContext.getResponseMessage();
//			String ret = redis.setNxTTL(
//					(PRE + act + "_" + applicationContext.getSessionId() + "_"
//							+ invocationContext.getRequestMessage().getSign()).getBytes(),
//					serializer.serializeFrom(responseMessage), blockTime);
//			if (!ret.equals("OK") && fastBackFail) {
//				responseMessage.setErrorCode(SystemErrorCode.FAST_RESPONSE_ERROR);
//				responseMessage.setMessageInfo("快速返回失败");
//				responseMessage.setResponseMap(null);
//			}
//		}
//	}
//}
