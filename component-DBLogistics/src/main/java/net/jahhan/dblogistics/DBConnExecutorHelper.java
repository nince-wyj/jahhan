package net.jahhan.dblogistics;

import java.util.HashMap;
import java.util.Map;

import net.jahhan.constant.enumeration.DBLogisticsConnectionType;
import net.jahhan.dblogistics.dbconnexecutor.DBLogisticNoneConnExecutor;
import net.jahhan.dblogistics.dbconnexecutor.DBLogisticReadConnExecutor;
import net.jahhan.dblogistics.dbconnexecutor.DBLogisticWriteConnExecutor;

public class DBConnExecutorHelper {
	private static Map<DBLogisticsConnectionType, DBConnExecutorHandler> map = new HashMap<>();

	static {
		map.put(DBLogisticsConnectionType.NONE, new DBLogisticNoneConnExecutor());
		map.put(DBLogisticsConnectionType.READ, new DBLogisticReadConnExecutor());
		map.put(DBLogisticsConnectionType.WRITE, new DBLogisticWriteConnExecutor());
	}

	public static DBConnExecutorHandler getDBConnExecutorHandler(DBLogisticsConnectionType dbLogisticsConnectionType) {
		return map.get(dbLogisticsConnectionType);
	}
}
