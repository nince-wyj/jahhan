package net.jahhan.init;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.jdbc.annotation.DBConnect;
import net.jahhan.jdbc.annotation.DBConnections;
import net.jahhan.jdbc.conn.DBConnFactory;
import net.jahhan.jdbc.constant.enumeration.DBConnectLevel;
import net.jahhan.jdbc.constant.enumeration.DBConnectStrategy;
import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
import net.jahhan.spi.common.BroadcastSender;
import net.jahhan.variable.BaseThreadVariable;
import net.jahhan.variable.DBVariable;

public class DBConnInterceptor implements MethodInterceptor {
	private static Logger logger = LoggerFactory.getLogger(DBConnInterceptor.class);
	@Inject
	private BroadcastSender broadcastSender;
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		DBVariable dbVariable = (DBVariable) DBVariable.getThreadVariable("db");
		DBConnections dBConnects = invocation.getMethod().getAnnotation(DBConnections.class);
		Object obj = null;
		if (null == dBConnects) {
			obj = invocation.proceed();
		} else {
			List<DBConnExecutorHolder> transactionExecutorList = new ArrayList<>();
			List<DBConnExecutorHolder> allExecutorList = new ArrayList<>();
			for (DBConnect dBConnect : dBConnects.value()) {
				if (dBConnect.level() == DBConnectLevel.NONE) {
					continue;
				}
				String dataSource = dBConnect.dataSource();
				if (dataSource.equals("")) {
					dataSource = DBConnFactory.getDefaultDataSource();
				}
				dbVariable.initConnectionData(dataSource);
				DBConnectStrategy dbConnectStrategy = dbVariable.getDBConnectStrategy(dataSource);
				dbVariable.setDBConnectStrategy(dataSource, dBConnect.value());
				DBConnExecutorHolder connExec = null;
				List<DBConnExecutorHolder> dbConnExecutorHolders = dbVariable.getDBConnExecutorHolders(dataSource);
				if (null != dbConnExecutorHolders) {
					for (DBConnExecutorHolder dbConnExecutorHolder : dbConnExecutorHolders) {
						if (dbConnExecutorHolder.getDbConnectLevel() == dBConnect.level()) {
							connExec = dbConnExecutorHolder;
							break;
						}
					}
				}

				if (null == connExec && dBConnect.transaction()) {
					connExec = new DBConnExecutorHolder(dataSource, dBConnect.level());
					connExec.beginConnection();
					dbVariable.addDBConnExecutorHolder(dataSource, connExec);
					dbVariable.setCurrentDBConnExecutorHolder(dataSource, connExec);
					dbVariable.setConnectionLevel(dataSource, dBConnect.level());
					transactionExecutorList.add(connExec);
				}

				allExecutorList.add(connExec);
				dbVariable.setDBConnectStrategy(dataSource, dbConnectStrategy);
			}
			try {
				obj = invocation.proceed();
				if (((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).isDbLazyCommit()) {

				} else {
					for (DBConnExecutorHolder connExec : transactionExecutorList) {
						connExec.commit();
					}
				}
			} catch (JahhanException e) {
				logger.error("DBConnHandler SystemException {}", e);
				throw e;
			} catch (Exception e) {
				logger.error("DBConnHandler exception {}", e);
				throw e;
			} catch (Error e) {
				logger.error("DBConnHandler error {}", e);
				throw e;
			} finally {
				if (((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).isDbLazyCommit()) {
					broadcastSender.setChainNode(((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId());
				} else {
					List<DBConnExecutorHolder> holders = new ArrayList<>();
					holders.addAll(allExecutorList);
					for (DBConnExecutorHolder connExec : holders) {
						connExec.close();
					}
				}
			}
		}
		return obj;
	}

}
