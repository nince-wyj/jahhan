package net.jahhan.web.action.actionhandler;

import javax.inject.Inject;

import org.slf4j.Logger;

import net.jahhan.api.ResponseMessage;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.constant.enumeration.RequestMethodEnum;
import net.jahhan.constant.enumeration.ResponseTypeEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.conn.DBConnFactory;
import net.jahhan.exception.BussinessException;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.web.action.ActionHandler;
import net.jahhan.web.action.WriteHelper;
import net.jahhan.web.action.annotation.ActionService;
import net.jahhan.web.action.annotation.HandlerAnnocation;

/**
 * @author nince
 */
@HandlerAnnocation(999)
public class WriteWorkHandler extends ActionHandler {

	private final Logger logger = LoggerFactory.getInstance().getLogger(WriteWorkHandler.class);

	private final RequestMethodEnum requestMethodEnum;
	private final CryptEnum responseSecurityType;
	private final String[] returnParameters;
	private final ResponseTypeEnum responseType;
	private final RequestMethodEnum requestMethod;
	@Inject
	private WriteHelper writeHelper;

	public WriteWorkHandler(ActionHandler actionHandler, ActionService actionService) {
		this.nextHandler = actionHandler;
		this.requestMethodEnum = actionService.requestMethod();
		this.responseSecurityType = actionService.responseEncrypt();
		this.returnParameters = actionService.returnParameters();
		this.responseType = actionService.responseType();
		this.requestMethod = actionService.requestMethod();
	}

	@Override
	public void execute() {
		if (!requestMethod.equals(RequestMethodEnum.BPMRECESSFILL)) {
			ApplicationContext applicationContext = ApplicationContext.CTX;
			InvocationContext invocationContext = applicationContext.getInvocationContext();
			ResponseMessage responseMessage = invocationContext.getResponseMessage();
			try {
				nextHandler.execute();
			} catch (BussinessException e) {
				logger.error("业务异常：", e);
				responseMessage.setMessageInfo(e.getMessage());
				responseMessage.setErrorCode(e.getCode());
			} catch (FrameworkException e) {
				logger.error("框架异常：", e);
				responseMessage.setMessageInfo(e.getMessage());
				responseMessage.setErrorCode(e.getCode());
			} catch (Exception e) {
				logger.error("未知异常：", e);
				responseMessage.setMessageInfo("未知错误");
				responseMessage.setErrorCode(SystemErrorCode.UNKOWN_ERROR);
			} catch (Error e) {
				logger.error("未知错误：", e);
				responseMessage.setMessageInfo("未知错误");
				responseMessage.setErrorCode(SystemErrorCode.UNKOWN_ERROR);
			} finally {
				switch (responseType) {
				case JSON:
					writeHelper.toWriteJson(responseMessage, requestMethodEnum, responseSecurityType, returnParameters);
					break;
				case CUSTOM:
					writeHelper.toWriteCustom(responseMessage, requestMethodEnum, responseSecurityType);
					break;
				case FILE:
					writeHelper.toWriteFile(responseMessage);
					break;
				case REDIRECT:
					writeHelper.redirect(responseMessage);
					break;
				default:
					break;
				}
				/*
				 * 全局关连接
				 */
				DBConnFactory.freeConns(invocationContext.getConnections());
			}
		} else {
			nextHandler.execute();
		}

	}
}
