package net.jahhan.init;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.annotation.DbConn;
import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.dbconnexecutor.DBConnExecutorFactory;
import net.jahhan.exception.BussinessException;
import net.jahhan.exception.FrameworkException;

public class DBConnInterceptor implements MethodInterceptor {
	private static Logger logger = LoggerFactory.getLogger(DBConnInterceptor.class);

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		DBConnectionType connectionType = invocationContext.getConnectionType();
		DbConn dbconn = invocation.getMethod().getAnnotation(DbConn.class);
		DBConnectionType value = dbconn.value();
		boolean transaction = dbconn.transaction();
		if (!(null != connectionType && connectionType != DBConnectionType.NONE
				&& connectionType != DBConnectionType.READ)) {
			transaction = true;
		}
		Object obj = null;
		if (null == value || null != connectionType && DBConnectionType.NONE != connectionType
				|| connectionType == value) {
			if (!transaction && null != value) {
				invocationContext.setConnectionType(value);
			}
			obj = invocation.proceed();
		} else {
			invocationContext.setConnectionType(value);

			DBConnExecutorFactory connExec = new DBConnExecutorFactory(value);

			try {
				if (transaction) {
					connExec.beginConnection();
				}
				obj = invocation.proceed();
				if (transaction) {
					connExec.endConnection();
				}
			} catch (BussinessException e) {
				if (transaction) {
					connExec.rollback();
				}
			} catch (FrameworkException e) {
				logger.error("DBConnHandler SystemException {}", e);
				if (transaction) {
					connExec.rollback();
				}
				throw e;
			} catch (Exception e) {
				logger.error("DBConnHandler exception {}", e);
				if (transaction) {
					connExec.rollback();
				}
			} catch (Error e) {
				logger.error("DBConnHandler error {}", e);
				if (transaction) {
					connExec.rollback();
				}
			} finally {
				connExec.close();
			}
		}
		return obj;
	}

}
