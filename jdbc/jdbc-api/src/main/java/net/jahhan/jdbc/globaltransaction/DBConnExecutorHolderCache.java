package net.jahhan.jdbc.globaltransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.jahhan.context.BaseVariable;
import net.jahhan.jdbc.context.DBVariable;
import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;

public class DBConnExecutorHolderCache {
	private static Map<String, Map<String, List<DBConnExecutorHolder>>> dbExecutorHolderMap = new ConcurrentHashMap<>();

	public static Map<String, List<DBConnExecutorHolder>> getDbExecutorHolders(String chainId) {
		Map<String, List<DBConnExecutorHolder>> map = dbExecutorHolderMap.get(chainId);
		if (null != map) {
			return map;
		}
		return null;
	}

	public static void setDbExecutorHolders(String chainId,
			Map<String, List<DBConnExecutorHolder>> dBConnExecutorHolders) {
		dbExecutorHolderMap.put(chainId, dBConnExecutorHolders);
	}

	public static void delDbExecutorHolders() {
		String chainId = BaseVariable.getBaseVariable().getChainId();
		dbExecutorHolderMap.remove(chainId);
	}

	public static void initDBVariable(String chainId) {
		Map<String, List<DBConnExecutorHolder>> map = dbExecutorHolderMap.get(chainId);
		if (null != map) {
			DBVariable dbVariable = DBVariable.getDBVariable();
			Set<String> dataSourceSet = map.keySet();
			for (String dataSource : dataSourceSet) {
				dbVariable.initConnectionData(dataSource);
				List<DBConnExecutorHolder> dBConnExecutorHolderlist = map.get(dataSource);
				if (null != dBConnExecutorHolderlist) {
					for (DBConnExecutorHolder dbConnExecutorHolder : dBConnExecutorHolderlist) {
						dbVariable.setCurrentDBConnExecutorHolder(dataSource, dbConnExecutorHolder);
					}
				}
			}
		}
	}

	public static void setDbExecutorHolders() {
		String chainId = BaseVariable.getBaseVariable().getChainId();
		Map<String, List<DBConnExecutorHolder>> map = new HashMap<>();
		DBVariable dbVariable = DBVariable.getDBVariable();
		Set<String> dataSources = dbVariable.getDataSources();
		for (String dataSource : dataSources) {
			List<DBConnExecutorHolder> dbConnExecutorHolders = dbVariable.getDBConnExecutorHolders(dataSource);
			if (null != dbConnExecutorHolders) {
				List<DBConnExecutorHolder> holders = new ArrayList<>();
				holders.addAll(dbConnExecutorHolders);
				map.put(dataSource, holders);
			}
		}
		dbExecutorHolderMap.put(chainId, map);
	}
}
