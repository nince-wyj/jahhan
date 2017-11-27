package net.jahhan.web.action.actionhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.constant.enumeration.DBLogisticsConnectionType;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.dblogistics.DBConnExecutorHandler;
import net.jahhan.dblogistics.DBConnExecutorHelper;
import net.jahhan.exception.BussinessException;
import net.jahhan.exception.FrameworkException;
import net.jahhan.web.action.ActionHandler;
import net.jahhan.web.action.annotation.ActionService;
import net.jahhan.web.action.annotation.HandlerAnnocation;

/**
 * 数据库管理
 * 
 * @author nince
 */
@HandlerAnnocation(110)
public class DBLogisticsHandler extends ActionHandler {
	private static Logger logger = LoggerFactory.getLogger(DBLogisticsHandler.class);
	private DBLogisticsConnectionType dbLogistics;

	public DBLogisticsHandler(ActionHandler actionHandler, ActionService actionService) {
		this.nextHandler = actionHandler;
		this.dbLogistics = actionService.dbLogistics();
	}

	@Override
	public void execute() {
		if (dbLogistics != DBLogisticsConnectionType.NONE) {
			BaseContext applicationContext = BaseContext.CTX;
			InvocationContext invocationContext = applicationContext.getInvocationContext();
			invocationContext.setDBLogisticsConnType(dbLogistics);
			DBConnExecutorHandler dbConnExecutorHandler = DBConnExecutorHelper.getDBConnExecutorHandler(dbLogistics);
			try {
				dbConnExecutorHandler.beginConnection();
				nextHandler.execute();
				dbConnExecutorHandler.commit();
			} catch (BussinessException e) {
				dbConnExecutorHandler.rollback();
				throw e;
			} catch (FrameworkException e) {
				logger.error("neo4jSession SystemException {}", e);
				dbConnExecutorHandler.rollback();
				throw e;
			} catch (Exception e) {
				logger.error("neo4jSession exception {}", e);
				dbConnExecutorHandler.rollback();
				throw e;
			} catch (Error e) {
				logger.error("neo4jSession error {}", e);
				dbConnExecutorHandler.rollback();
				throw e;
			} finally {
				dbConnExecutorHandler.close();
			}
		} else {
			nextHandler.execute();
		}

	}
}
