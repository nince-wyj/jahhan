package net.jahhan.extension.afterWriteHandler;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.alibaba.dubbo.common.extension.Activate;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.context.BaseVariable;
import net.jahhan.exception.JahhanException;
import net.jahhan.globalTransaction.LockParamHolder;
import net.jahhan.globalTransaction.LockStatus;
import net.jahhan.globalTransaction.LockThreadStatus;
import net.jahhan.globalTransaction.WaitingThreadHolder;
import net.jahhan.jdbc.constant.JDBCConstants;
import net.jahhan.jdbc.context.DBVariable;
import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
import net.jahhan.spi.AfterWriteHandler;
import net.jahhan.spi.BroadcastSender;

@Activate(order = 0)
@Extension("globalSyncWait")
@Singleton
public class GlobalSyncWaitAfterWriteHandler implements AfterWriteHandler {
	@Inject
	private BroadcastSender broadcastSender;

	@Override
	public void exec() throws JahhanException {
		BaseVariable baseVariable = BaseVariable.getBaseVariable();
		if (null != baseVariable && baseVariable.isDbLazyCommit() && !baseVariable.isGlobalSyncTransactionHold()) {
			long holdTimeOut = JDBCConstants.getHoldTimeOut();
			DBVariable dbVariable = DBVariable.getDBVariable();
			Set<String> dataSources = dbVariable.getDataSources();
			long startTime = 0;
			for (String dataSource : dataSources) {
				List<DBConnExecutorHolder> dbConnExecutorHolders = dbVariable.getDBConnExecutorHolders(dataSource);
				if (null != dbConnExecutorHolders) {
					for (DBConnExecutorHolder dBConnExecutorHolder : dbConnExecutorHolders) {
						if (dBConnExecutorHolder.getStartTime() > startTime) {
							startTime = dBConnExecutorHolder.getStartTime();
						}
					}
				}
			}
			long waitTime = holdTimeOut * 1000 - (System.currentTimeMillis() - startTime);
			String chainId = baseVariable.getChainId();
			if (waitTime > 0) {
				String lock = "GOBAL_TRANSACTION_" + chainId;
				LockParamHolder.newChainLock(chainId);
				WaitingThreadHolder.registWaitingThread(lock, chainId, waitTime);
			} else {
				broadcastSender.send("TRANSACTION_ROLLBACK", BaseVariable.getBaseVariable().getChainId());
				LockStatus chainLock = LockParamHolder.getChainLock(chainId);
				if (null != chainLock) {
					chainLock.setStatus(LockThreadStatus.ERROR);
				}
			}
		}

	}
}
