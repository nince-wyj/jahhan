package net.jahhan.jdbc.conn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.DataSourcePoolInit;

@Slf4j
public abstract class DataSourcePool implements DataSourcePoolInit {
	@Getter
	@Setter
	private String defaultDataSource;

	protected Map<String, DataSourceConnectionPool> dataSourceConnectionMap = new ConcurrentHashMap<>();

	public Connection getHoldConnection(String dataSource) throws SQLException {
		return dataSourceConnectionMap.get(dataSource).getHoldConnection();
	}

	public Connection getHoldConnection() throws SQLException {
		return getHoldConnection(defaultDataSource);
	}
	
	public Connection getWriteConnection(String dataSource) throws SQLException {
		return dataSourceConnectionMap.get(dataSource).getWriteConnection();
	}

	public Connection getWriteConnection() throws SQLException {
		return getWriteConnection(defaultDataSource);
	}

	public Connection getBatchConnection(String dataSource) throws SQLException {
		return dataSourceConnectionMap.get(dataSource).getBatchConnection();
	}

	public Connection getBatchConnection() throws SQLException {
		return getBatchConnection(defaultDataSource);
	}

	public Connection getReadConnection(String dataSource) throws SQLException {
		return dataSourceConnectionMap.get(dataSource).getReadConnection();
	}

	public Connection getReadConnection() throws SQLException {
		return getReadConnection(defaultDataSource);
	}

	public String getStatus() throws SQLException {
		StringBuilder sb = new StringBuilder();
		Collection<DataSourceConnectionPool> values = dataSourceConnectionMap.values();
		for (DataSourceConnectionPool dataSourceConnectionPool : values) {
			sb.append(dataSourceConnectionPool.getStatus());
		}
		return sb.toString();
	}

	public static void freeConnection(Connection dbConn) throws SQLException {
		if (dbConn != null && !dbConn.isClosed()) {
			dbConn.close();
		}
	}

	@Override
	public void init() {
		log.debug("DataSourcePool init===========================================" + this.getClass().getClassLoader());
		try {
			Properties property = PropertiesUtil
					.getProperties(PropertiesUtil.getProperties("base").getProperty("jdbc.fileName", "jdbc"));
			defaultDataSource = property.getProperty("default.source");
			String dataSourceString = property.getProperty("data.source");
			String[] dataSources = dataSourceString.split(",");
			for (int i = 0; i < dataSources.length; i++) {
				String dataSource = dataSources[i];
				DataSourceConnectionPool dataSourceConnectionPool = new DataSourceConnectionPool();
				dataSourceConnectionPool.setDataSource(dataSource);
				dataSourceConnectionMap.put(dataSource, dataSourceConnectionPool);
				initDataSource(dataSource, property);
			}
		} catch (Exception ex) {
			throw new JahhanException("加载系统配置出错");
		}
	}
}
