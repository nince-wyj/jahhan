package net.jahhan.init;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.annotation.Transaction;
import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.dbconnexecutor.DBConnExecutorFactory;
import net.jahhan.exception.BussinessException;
import net.jahhan.exception.FrameworkException;

public class TransactionInterceptor implements MethodInterceptor {
	private static Logger logger = LoggerFactory.getLogger(TransactionInterceptor.class);

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		BaseContext applicationContext = BaseContext.CTX;
		InvocationContext invocationContext = applicationContext.getInvocationContext();
		DBConnectionType connectionType = invocationContext.getConnectionType();
		Object obj = null;
		Transaction transaction = invocation.getMethod().getAnnotation(Transaction.class);
		if (null != connectionType && connectionType != DBConnectionType.NONE && connectionType != DBConnectionType.READ
				&& null != transaction) {
			DBConnExecutorFactory connExec = new DBConnExecutorFactory(connectionType);
			try {
				connExec.beginConnection();
				obj = invocation.proceed();
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
				throw e;
			} catch (Error e) {
				logger.error("DBConnHandler error {}", e);
				connExec.rollback();
				throw e;
			} finally {
				connExec.close();
			}
		} else {
			obj = invocation.proceed();
		}
		return obj;
	}

}
