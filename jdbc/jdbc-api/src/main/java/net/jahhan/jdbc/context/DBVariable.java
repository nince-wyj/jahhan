package net.jahhan.jdbc.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.Variable;
import net.jahhan.jdbc.constant.enumeration.DBConnectLevel;
import net.jahhan.jdbc.constant.enumeration.DBConnectStrategy;
import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
import net.jahhan.jdbc.event.DBEvent;

/**
 * 数据库线程局部变量
 * 
 */
public class DBVariable extends Variable {
	@Data
	public class ConnectionData {
		private List<DBConnExecutorHolder> executorList;
		private DBConnectStrategy dbConnectStrategy;
		private DBConnectLevel dbConnectLevel = DBConnectLevel.NONE;
		private DBConnExecutorHolder currentDBConnExecutorHolder;
	}

	// 数据库连接
	private Map<String, ConnectionData> connMap = new HashMap<>();

	private ConnectionData getConnectionData(String dataSource) {
		return connMap.get(dataSource);
	}

	public Set<String> getDataSources() {
		return connMap.keySet();
	}

	public ConnectionData initConnectionData(String dataSource) {
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			connectionData = new ConnectionData();
			connMap.put(dataSource, connectionData);
		}
		return connectionData;
	}

	public List<DBConnExecutorHolder> getDBConnExecutorHolders(String dataSource) {
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			return null;
		}
		return connectionData.getExecutorList();
	}

	public void addDBConnExecutorHolder(String dataSource, DBConnExecutorHolder con) {
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			return;
		}
		List<DBConnExecutorHolder> list = connectionData.getExecutorList();
		if (null == list) {
			list = new ArrayList<>();
			connMap.get(dataSource).setExecutorList(list);
		}
		list.add(con);
	}

	public void removeDBConnExecutorHolder(String dataSource, DBConnExecutorHolder dbConnExecutorHolder) {
		if (this.getCurrentDBConnExecutorHolder(dataSource) == dbConnExecutorHolder) {
			this.setCurrentDBConnExecutorHolder(dataSource, null);
		}
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			return;
		}
		List<DBConnExecutorHolder> list = connectionData.getExecutorList();
		if (list == null) {
			return;
		}
		list.remove(dbConnExecutorHolder);
	}

	public DBConnExecutorHolder getCurrentDBConnExecutorHolder(String dataSource) {
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			return null;
		}
		return connectionData.getCurrentDBConnExecutorHolder();
	}

	public void setCurrentDBConnExecutorHolder(String dataSource, DBConnExecutorHolder dbConnExecutorHolder) {
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			return;
		}
		connectionData.setCurrentDBConnExecutorHolder(dbConnExecutorHolder);
	}

	public void setConnectionLevel(String dataSource, DBConnectLevel dbConnectLevel) {
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			return;
		}
		connectionData.setDbConnectLevel(dbConnectLevel);
	}

	public boolean isWriteConnection(String dataSource) {
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			return false;
		}
		return connectionData.getDbConnectLevel().getLevel() > DBConnectLevel.READ.getLevel();
	}

	public DBConnectLevel getDbConnectLevel(String dataSource) {
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			return DBConnectLevel.NONE;
		}
		return connectionData.getDbConnectLevel();
	}

	public DBConnectStrategy getDBConnectStrategy(String dataSource) {
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			return null;
		}
		return connectionData.getDbConnectStrategy();
	}

	public void setDBConnectStrategy(String dataSource, DBConnectStrategy dbConnectStrategy) {
		ConnectionData connectionData = getConnectionData(dataSource);
		if (null == connectionData) {
			return;
		}
		connectionData.setDbConnectStrategy(dbConnectStrategy);
	}

	// 线程缓存
	private Map<String, Object> writeCache = new HashMap<>();

	private Map<String, Object> sessions = new HashMap<>();

	private List<DBEvent> events = new ArrayList<>();

	private List<String> modifyKeys = new ArrayList<>();

	public void setDBEvent(DBEvent event) {
		events.add(event);
	}

	public List<DBEvent> getEvents() {
		return events;
	}

	public Object getSession(String sessionName) {
		return sessions.get(sessionName);
	}

	public void setSession(String sessionName, Object session) {
		this.sessions.put(sessionName, session);
	}

	public void addPojo(Class<?> pojoClass, String id, Object pojo) {
		writeCache.put(pojoClass.getSimpleName() + id, pojo);
	}

	public void delPojo(Class<?> pojoClass, String id) {
		writeCache.remove(pojoClass.getSimpleName() + id);
		modifyKeys.add(pojoClass.getSimpleName() + id);
	}

	public boolean isDeletePojo(Class<?> pojoClass, String id) {
		return modifyKeys.contains(pojoClass.getSimpleName() + id);
	}

	public Object getLocalCachePojo(Class<?> pojoClass, String id) {
		return writeCache.get(pojoClass.getSimpleName() + id);
	}

	public void clearLocalCache() {
		writeCache.clear();
		modifyKeys.clear();
	}

	public static DBVariable getDBVariable() {
		DBVariable variable = (DBVariable) BaseContext.CTX.getVariableContext().getVariable("db");
		if (null == variable) {
			variable = new DBVariable();
			BaseContext.CTX.getVariableContext().putVariable("db", variable);
		}
		return variable;
	}
}
