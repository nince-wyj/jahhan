package net.jahhan.web.action.actionhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.dbconnexecutor.DBConnExecutorFactory;
import net.jahhan.exception.BussinessException;
import net.jahhan.exception.FrameworkException;
import net.jahhan.web.action.ActionHandler;
import net.jahhan.web.action.annotation.ActionService;
import net.jahhan.web.action.annotation.HandlerAnnocation;

/**
 * 数据库连接管理
 * 
 * @author nince
 */
@HandlerAnnocation(100)
public class DBConnHandler extends ActionHandler {
	private static Logger logger = LoggerFactory.getLogger(DBConnHandler.class);

	private DBConnectionType dBConnectionType;

	public DBConnHandler(ActionHandler actionHandler, ActionService actionService) {
		this.nextHandler = actionHandler;
		this.dBConnectionType = actionService.dbConnType();
	}

	@Override
	public void execute() {
		BaseContext applicationContext = BaseContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		invocationContext.setConnectionType(dBConnectionType);
		DBConnExecutorFactory connExec = new DBConnExecutorFactory(dBConnectionType);
		try {
			connExec.beginConnection();
			nextHandler.execute();
			connExec.endConnection();
		} catch (BussinessException e) {
			connExec.rollback();
			throw e;
		} catch (FrameworkException e) {
			logger.error("DBConnHandler SystemException {}", e);
			connExec.rollback();
			throw e;
		} catch (Exception e) {
			logger.error("DBConnHandler exception {}", e);
			connExec.rollback();
		} catch (Error e) {
			logger.error("DBConnHandler error {}", e);
			connExec.rollback();
		} finally {
			connExec.close();
		}
	}
}
