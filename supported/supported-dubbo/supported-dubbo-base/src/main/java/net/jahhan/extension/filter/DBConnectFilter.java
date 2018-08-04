package net.jahhan.extension.filter;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.Extension;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

import net.jahhan.common.extension.annotation.GlobalSyncTransaction;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.BaseVariable;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.ExtensionUtil;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.config.ServiceImplCache;
import net.jahhan.jdbc.annotation.DBConnect;
import net.jahhan.jdbc.annotation.DBConnections;
import net.jahhan.jdbc.conn.DBConnFactory;
import net.jahhan.jdbc.context.DBVariable;
import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
import net.jahhan.register.ClusterMessageHolder;
import net.jahhan.spi.common.BroadcastSender;

@Activate(group = Constants.PROVIDER, order = 1000)
@Extension("dbConnect")
@Singleton
public class DBConnectFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation inv) throws RpcException {
		BroadcastSender broadcastSender = ExtensionUtil.getExtension(BroadcastSender.class);
		String interfaceClassName = invoker.getUrl().getParameter("interface");

		String methodName = inv.getMethodName();
		Method implMethod = null;
		try {
			implMethod = ServiceImplCache.getInstance().getRef(interfaceClassName).getDeclaredMethod(methodName,
					inv.getParameterTypes());
		} catch (NoSuchMethodException | SecurityException e) {
			throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
		}
		DBConnections dBConnects = implMethod.getAnnotation(DBConnections.class);
		GlobalSyncTransaction globalSyncTransaction = implMethod.getAnnotation(GlobalSyncTransaction.class);
		BaseVariable baseVariable = BaseVariable.getBaseVariable();
		if (null != globalSyncTransaction) {
			if (!baseVariable.isDbLazyCommit()) {
				baseVariable.setDbLazyCommit(true);
				baseVariable.setGlobalSyncTransactionHold(true);
			}
		}
		DBVariable dbVariable = DBVariable.getDBVariable();
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
		Result result = null;
		boolean commit = true;
		try {
			result = invoker.invoke(inv);
		} catch (JahhanException e) {
			commit = false;
			throw e;
		} catch (Exception e) {
			commit = false;
			throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
		} finally {
			if (null != result) {
				Throwable exception = result.getException();
				if (null != exception) {
					commit = false;
				}
			}
			String chainId = BaseVariable.getBaseVariable().getChainId();
			if (baseVariable.isGlobalSyncTransactionHold()) {
				if (null != result) {
					Throwable exception = result.getException();
					if (null != exception) {
						commit = false;
						broadcastSender.send("TRANSACTION_ROLLBACK", chainId);
					} else {
						Set<String> chainNode = broadcastSender.getChainNode(chainId);
						if (allContains(chainNode)) {
							broadcastSender.send("TRANSACTION_COMMIT", chainId);
						} else {
							commit = false;
							broadcastSender.send("TRANSACTION_ROLLBACK", chainId);
						}
					}
					broadcastSender.removeChain(chainId);
					closeAllConnection(commit);
				}
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
		DBVariable dbVariable = DBVariable.getDBVariable();
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