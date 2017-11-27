package net.jahhan.web.action.actionhandler;

import org.slf4j.Logger;

import net.jahhan.api.Action;
import net.jahhan.api.ResponseMessage;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.exception.BussinessException;
import net.jahhan.exception.FrameworkException;
import net.jahhan.exception.NoRollBackException;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.web.action.ActionHandler;

/**
 * 框架和业务层绝对解藕，包装action
 * 
 * @author nince
 */
public class ActionWorkHandler extends ActionHandler {
	private final Logger logger = LoggerFactory.getInstance().getLogger(ActionWorkHandler.class);
	private Action action;

	public ActionWorkHandler(Action action) {
		this.action = action;
	}

	@Override
	public void execute() {
		BaseContext applicationContext = BaseContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		ResponseMessage responseMessage = invocationContext.getResponseMessage();
		try {
			action.execute(invocationContext.getRequestMessage(), responseMessage);
		} catch (NoRollBackException e) {
			logger.error("免回滚业务异常：", e.getMessage());
			responseMessage.setMessageInfo(e.getMessage());
			responseMessage.setErrorCode(e.getCode());
		} catch (BussinessException e) {
			logger.error("业务异常：", e.getMessage());
			responseMessage.setMessageInfo(e.getMessage());
			responseMessage.setErrorCode(e.getCode());
			throw e;
		} catch (FrameworkException e) {
			throw e;
		} catch (Exception e) {
			FrameworkException.throwException(SystemErrorCode.UNKOWN_ERROR, "未知错误", e);
		} catch (Error e) {
			FrameworkException.throwException(SystemErrorCode.UNKOWN_ERROR, "未知错误", e);
		}
	}
}
