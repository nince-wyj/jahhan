package net.jahhan.jdbc.globaltransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
import net.jahhan.variable.BaseThreadVariable;
import net.jahhan.variable.DBVariable;

public class DBConnExecutorHolderCache {
	private static Map<String, Map<String, List<DBConnExecutorHolder>>> dbExecutorHolderMap = new ConcurrentHashMap<>();
	private static Map<String, Map<String, DBConnExecutorHolder>> currentDbExecutorHolderMap = new ConcurrentHashMap<>();
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				Set<String> chainIdSet = dbExecutorHolderMap.keySet();
				for (String chainId : chainIdSet) {
					Map<String, List<DBConnExecutorHolder>> map = dbExecutorHolderMap.get(chainId);
					Set<String> sourceSet = map.keySet();
					for (String source : sourceSet) {
						List<DBConnExecutorHolder> list = map.get(source);
						for (DBConnExecutorHolder dBConnExecutorHolder : list) {
							dBConnExecutorHolder.rollback();
							dBConnExecutorHolder.close();
						}
					}
				}
			}
		}));
	}

	public static Map<String, List<DBConnExecutorHolder>> getDbExecutorHolders(String chainId) {
		Map<String, List<DBConnExecutorHolder>> map = dbExecutorHolderMap.remove(chainId);
		if (null != map) {
			return map;
		}
		return null;
	}
	
	public static Map<String, DBConnExecutorHolder> getCurrentDbExecutorHolders(String chainId) {
		Map<String, DBConnExecutorHolder> map = currentDbExecutorHolderMap.remove(chainId);
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
		String chainId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
		dbExecutorHolderMap.remove(chainId);
	}

	public static boolean initDBVariable(String chainId) {
		Map<String, List<DBConnExecutorHolder>> map = dbExecutorHolderMap.remove(chainId);
		Map<String, DBConnExecutorHolder> currentMap = currentDbExecutorHolderMap.remove(chainId);
		if (null != map && !map.isEmpty()) {
			DBVariable dbVariable = (DBVariable) DBVariable.getThreadVariable("db");
			Set<String> dataSourceSet = map.keySet();
			for (String dataSource : dataSourceSet) {
				dbVariable.initConnectionData(dataSource);
				List<DBConnExecutorHolder> dBConnExecutorHolderlist = map.get(dataSource);
				if (null != dBConnExecutorHolderlist) {
					for (DBConnExecutorHolder dbConnExecutorHolder : dBConnExecutorHolderlist) {
						dbVariable.addDBConnExecutorHolder(dataSource, dbConnExecutorHolder);
					}
				}
				DBConnExecutorHolder dbConnExecutorHolder = currentMap.get(dataSource);
				dbVariable.setCurrentDBConnExecutorHolder(dataSource, dbConnExecutorHolder);
			}
			return true;
		}
		return false;
	}

	public static void setDbExecutorHolders() {
		String chainId = ((BaseThreadVariable) BaseThreadVariable.getThreadVariable("base")).getChainId();
		Map<String, List<DBConnExecutorHolder>> map = new HashMap<>();
		Map<String, DBConnExecutorHolder> currentMap = new HashMap<>();
		DBVariable dbVariable = (DBVariable) DBVariable.getThreadVariable("db");
		Set<String> dataSources = dbVariable.getDataSources();
		for (String dataSource : dataSources) {
			List<DBConnExecutorHolder> dbConnExecutorHolders = dbVariable.getDBConnExecutorHolders(dataSource);
			if (null != dbConnExecutorHolders) {
				List<DBConnExecutorHolder> holders = new ArrayList<>();
				holders.addAll(dbConnExecutorHolders);
				map.put(dataSource, holders);
			}
			currentMap.put(dataSource, dbVariable.getCurrentDBConnExecutorHolder(dataSource));
		}
		dbExecutorHolderMap.put(chainId, map);
		currentDbExecutorHolderMap.put(chainId, currentMap);
	}
}
