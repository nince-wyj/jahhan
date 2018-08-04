package net.jahhan.init;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.context.BaseVariable;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.exception.NoRollBackException;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.jdbc.annotation.Transaction;
import net.jahhan.jdbc.context.DBVariable;
import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
import net.jahhan.spi.common.BroadcastSender;

@Slf4j
public class TransactionInterceptor implements MethodInterceptor {
	@Inject
	private BroadcastSender broadcastSender;
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		DBVariable dbVariable = DBVariable.getDBVariable();
		Object obj = null;
		Transaction transaction = invocation.getMethod().getAnnotation(Transaction.class);
		if (null != transaction) {
			String[] dataSources = transaction.value();
			List<DBConnExecutorHolder> dbConnExecutorHolderList = new ArrayList<>();

			for (String dataSource : dataSources) {
				DBConnExecutorHolder connExec = dbVariable.getCurrentDBConnExecutorHolder(dataSource);
				if (null == connExec) {
					connExec = new DBConnExecutorHolder(dataSource, dbVariable.getDbConnectLevel(dataSource));
					connExec.beginConnection();
					dbVariable.addDBConnExecutorHolder(dataSource, connExec);
				}
				dbConnExecutorHolderList.add(connExec);
			}
			try {
				obj = invocation.proceed();
				if (BaseVariable.getBaseVariable().isDbLazyCommit() && !transaction.globalRespond()) {
					broadcastSender.setChainNode(BaseVariable.getBaseVariable().getChainId());
				} else {
					for (DBConnExecutorHolder connExec : dbConnExecutorHolderList) {
						connExec.commit();
					}
				}
			} catch (NoRollBackException e) {
				log.warn("DBConnHandler NoRollBackException {}", e);
			} catch (JahhanException e) {
				LogUtil.error("DBConnHandler FrameWorkXException {}", e);
				for (DBConnExecutorHolder connExec : dbConnExecutorHolderList) {
					connExec.rollback();
				}
				throw e;
			} catch (Exception e) {
				LogUtil.error("DBConnHandler exception {}", e);
				for (DBConnExecutorHolder connExec : dbConnExecutorHolderList) {
					connExec.rollback();
				}
				throw e;
			} catch (Error e) {
				LogUtil.error("DBConnHandler error {}", e);
				for (DBConnExecutorHolder connExec : dbConnExecutorHolderList) {
					connExec.rollback();
				}
				throw e;
			} finally {
				if (BaseVariable.getBaseVariable().isDbLazyCommit() && !transaction.globalRespond()) {
					broadcastSender.setChainNode(BaseVariable.getBaseVariable().getChainId());
				} else {
					for (DBConnExecutorHolder connExec : dbConnExecutorHolderList) {
						connExec.close();
					}
				}
			}
		} else {
			obj = invocation.proceed();
		}
		return obj;
	}

}
