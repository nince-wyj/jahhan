package net.jahhan.init;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.annotation.DbLogisticsConn;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.DBLogisticsConnectionType;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.dblogistics.DBConnExecutorHandler;
import net.jahhan.dblogistics.DBConnExecutorHelper;
import net.jahhan.exception.BussinessException;
import net.jahhan.exception.FrameworkException;

public class DbLogisticsConnectionInterceptor implements MethodInterceptor {
	private static Logger logger = LoggerFactory.getLogger(DbLogisticsConnectionInterceptor.class);

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		BaseContext applicationContext = BaseContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		DBLogisticsConnectionType dbLogisticsConnType = invocationContext.getDBLogisticsConnType();
		DbLogisticsConn dbconn = invocation.getMethod().getAnnotation(DbLogisticsConn.class);
		DBLogisticsConnectionType value = dbconn.value();
		Object obj = null;
		if (null != dbLogisticsConnType && null != value) {
			FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "数据库连接配置错误！");
		}
		if (null == value) {
			value = DBLogisticsConnectionType.NONE;
		}
		DBConnExecutorHandler dbConnExecutorHandler = DBConnExecutorHelper.getDBConnExecutorHandler(value);
		try {
			dbConnExecutorHandler.beginConnection();
			obj = invocation.proceed();
			dbConnExecutorHandler.commit();
		} catch (BussinessException e) {
			dbConnExecutorHandler.rollback();
		} catch (FrameworkException e) {
			logger.error("DBConnHandler SystemException {}", e);
			dbConnExecutorHandler.rollback();
			throw e;
		} catch (Exception e) {
			logger.error("DBConnHandler exception {}", e);
			dbConnExecutorHandler.rollback();
		} catch (Error e) {
			logger.error("DBConnHandler error {}", e);
			dbConnExecutorHandler.rollback();
		} finally {
			dbConnExecutorHandler.close();
		}
		return obj;
	}
}
