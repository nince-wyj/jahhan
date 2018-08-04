package net.jahhan.jdbc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.jahhan.common.extension.context.BaseVariable;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.jdbc.context.DBVariable;
import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
import net.jahhan.spi.common.BroadcastSender;

@Singleton
public class DBConnExecutorHolderUtil {
	@Inject
	private BroadcastSender broadcastSender;

	public void commit(boolean commit) {
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
					} catch (Exception e) {
						LogUtil.error("全局事务提交失败！！！！！！！！！！！", e);
						broadcastSender.send("TRANSACTION_ROLLBACK", BaseVariable.getBaseVariable().getChainId());
						dbConnExecutorHolder.rollback();
					} finally {
						try {
							dbConnExecutorHolder.close();
						} catch (Exception e) {
							LogUtil.error("全局事务关闭连接失败！！！！！！！！！！！", e);
						}
					}
				}
			}
		}
	}
}
