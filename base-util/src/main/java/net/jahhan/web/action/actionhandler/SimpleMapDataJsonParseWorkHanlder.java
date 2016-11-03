package net.jahhan.web.action.actionhandler;

import java.util.Map;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.exception.FrameworkException;
import net.jahhan.web.action.ActionHandler;
import net.jahhan.web.action.annotation.ActionService;
import net.jahhan.web.action.annotation.HandlerAnnocation;

/**
 * @author nince
 */
@HandlerAnnocation(600)
public class SimpleMapDataJsonParseWorkHanlder extends ActionHandler {

    private String[] parameters;

    public SimpleMapDataJsonParseWorkHanlder(ActionHandler actionHandler,
    		ActionService actionService) {
		this.nextHandler = actionHandler;
		this.parameters = actionService.importantParameters();
	}

    @Override
    public void execute() {
        ApplicationContext applicationContext = ApplicationContext.CTX;
        InvocationContext invocationContext = applicationContext.getInvocationContext();
        Map<String, Object> requestMap = invocationContext.getRequestMessage().getRequestMap();
        for (String parameter : parameters) {
            if (requestMap.containsKey(parameter)) {
                continue;     
            }
            FrameworkException.throwException(SystemErrorCode.PARAMETER_ERROR, parameter+"参数没有指定");
        }
        nextHandler.execute();
    }
}
