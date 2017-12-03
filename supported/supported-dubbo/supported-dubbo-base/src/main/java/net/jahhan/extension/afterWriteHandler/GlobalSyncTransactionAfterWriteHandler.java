//package net.jahhan.extension.afterWriteHandler;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//
//import com.alibaba.dubbo.common.extension.Activate;
//
//import net.jahhan.common.extension.annotation.Extension;
//import net.jahhan.common.extension.utils.LogUtil;
//import net.jahhan.context.BaseContext;
//import net.jahhan.context.BaseVariable;
//import net.jahhan.context.VariableContext;
//import net.jahhan.exception.JahhanException;
//import net.jahhan.globalTransaction.LockParamHolder;
//import net.jahhan.globalTransaction.LockThreadStatus;
//import net.jahhan.jdbc.context.DBVariable;
//import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
//import net.jahhan.spi.AfterWriteHandler;
//import net.jahhan.spi.common.BroadcastSender;
//
//@Activate(order = 100)
//@Extension("globalSyncTransaction")
//@Singleton
//public class GlobalSyncTransactionAfterWriteHandler implements AfterWriteHandler {
//	@Inject
//	private BroadcastSender broadcastSender;
//
//	@Override
//	public void exec() throws JahhanException {
//		BaseVariable baseVariable = BaseVariable.getBaseVariable();
//		if (null != baseVariable && baseVariable.isDbLazyCommit() && !baseVariable.isGlobalSyncTransactionHold()) {
//			String chainId = baseVariable.getChainId();
//			LockThreadStatus chainLockStatus = LockParamHolder.getChainLockStatus(chainId);
//
//			DBVariable dbVariable = DBVariable.getDBVariable();
//			Set<String> dataSources = dbVariable.getDataSources();
//			for (String dataSource : dataSources) {
//				List<DBConnExecutorHolder> dbConnExecutorHolders = dbVariable.getDBConnExecutorHolders(dataSource);
//				if (null != dbConnExecutorHolders) {
//					List<DBConnExecutorHolder> holders = new ArrayList<>();
//					holders.addAll(dbConnExecutorHolders);
//					for (DBConnExecutorHolder dbConnExecutorHolder : holders) {
//						try {
//							if (chainLockStatus == LockThreadStatus.WEAKUP) {
//								dbConnExecutorHolder.commit();
//							} else {
//								dbConnExecutorHolder.rollback();
//							}
//						} catch (Exception e) {
//							LogUtil.error("全局事务提交失败！！！！！！！！！！！", e);
//							broadcastSender.send("TRANSACTION_ROLLBACK", BaseVariable.getBaseVariable().getChainId());
//							dbConnExecutorHolder.rollback();
//						} finally {
//							try {
//								dbConnExecutorHolder.close();
//							} catch (Exception e) {
//								LogUtil.error("全局事务关闭连接失败！！！！！！！！！！！", e);
//							}
//						}
//					}
//					LockParamHolder.removeChainLock(chainId);
//				}
//			}
//		}
//		VariableContext variableContext = BaseContext.CTX.getVariableContext();
//		if (null != variableContext) {
//			variableContext.reset();
//		}
//	}
//}
