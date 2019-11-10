package net.jahhan.extension.filter;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import net.jahhan.common.extension.annotation.GlobalSyncTransaction;
import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.ExtensionUtil;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.jdbc.annotation.DBConnect;
import net.jahhan.jdbc.annotation.DBConnections;
import net.jahhan.jdbc.conn.DBConnFactory;
import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
import net.jahhan.register.ClusterMessageHolder;
import net.jahhan.spi.common.BroadcastSender;
import net.jahhan.spring.aspect.Filter;
import net.jahhan.spring.aspect.Invocation;
import net.jahhan.spring.aspect.Invoker;
import net.jahhan.variable.BaseThreadVariable;
import net.jahhan.variable.DBVariable;

@Singleton
@Order(1000)
public class DBConnectFilter implements Filter {

	public Object invoke(Invoker invoker, Invocation invocation) throws JahhanException {
		BroadcastSender broadcastSender = ExtensionUtil.getExtension(BroadcastSender.class);

		Method method = invocation.getMethod();
		DBConnections dBConnects = method.getAnnotation(DBConnections.class);
		GlobalSyncTransaction globalSyncTransaction = method.getAnnotation(GlobalSyncTransaction.class);
		BaseThreadVariable baseVariable = (BaseThreadVariable) BaseThreadVariable.getThreadVariable("base");
		if (null != globalSyncTransaction) {
			if (!baseVariable.isDbLazyCommit()) {
				baseVariable.setDbLazyCommit(true);
				baseVariable.setGlobalSyncTransactionHold(true);
			}
		}
		DBVariable dbVariable = (DBVariable) DBVariable.getThreadVariable("db");
		if (null != dBConnects && dBConnects.value().length > 0) {
			for (DBConnect dBConnect : dBConnects.value()) {
				String dataSource = dBConnect.dataSource();
				if (dataSource.equals("")) {
					dataSource = DBConnFactory.getDefaultDataSource();
				}
				dbVariable.initConnectionData(dataSource);
				dbVariable.setDBConnectStrategy(dataSource, dBConnect.value());
			}
		}
		String chainId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
		Object result = null;
		boolean commit = true;
		try {
			result = invoker.invoke(invocation);
		} catch (JahhanException e) {
			commit = false;
			broadcastSender.send("TRANSACTION_ROLLBACK", chainId);
			throw e;
		} catch (Exception e) {
			commit = false;
			broadcastSender.send("TRANSACTION_ROLLBACK", chainId);
			throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
		} finally {
			if (baseVariable.isGlobalSyncTransactionHold()) {
				Set<String> chainNode = broadcastSender.getChainNode(chainId);
				if (allContains(chainNode)) {
					broadcastSender.send("TRANSACTION_COMMIT", chainId);
				} else {
					commit = false;
					broadcastSender.send("TRANSACTION_ROLLBACK", chainId);
				}
				broadcastSender.removeChain(chainId);
				closeAllConnection(commit);
			} else if (baseVariable.isDbLazyCommit()) {
				broadcastSender.setChainNode(chainId);
			} else {
				closeAllConnection(commit);
			}
		}
		return result;
	}

	private boolean allContains(Set<String> chainNode) {
		ClusterMessageHolder instance = BaseContext.CTX.getInjector().getInstance(ClusterMessageHolder.class);
		for (String nodeId : chainNode) {
			if (!instance.contains(nodeId)) {
				return false;
			}
		}
		return true;
	}

	private void closeAllConnection(boolean commit) {
		DBVariable dbVariable = (DBVariable) DBVariable.getThreadVariable("db");
		Set<String> dataSources = dbVariable.getDataSources();
		for (String dataSource : dataSources) {
			List<DBConnExecutorHolder> dbConnExecutorHolders = dbVariable.getDBConnExecutorHolders(dataSource);
			if (null != dbConnExecutorHolders) {
				List<DBConnExecutorHolder> holders = new ArrayList<>();
				holders.addAll(dbConnExecutorHolders);
				for (DBConnExecutorHolder dbConnExecutorHolder : holders) {
					try {
						if (commit) {
							dbConnExecutorHolder.commit();
						} else {
							dbConnExecutorHolder.rollback();
						}
						dbConnExecutorHolder.close();
					} catch (SQLException e) {
						LogUtil.error("尝试全局关闭连接失败。", e);
					}
				}
			}
		}
	}
}