package net.jahhan.jdbc.dbconnexecutor;

import java.sql.Connection;
import java.sql.SQLException;

import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.jdbc.DBConnExecutorHandler;
import net.jahhan.jdbc.conn.DBConnFactory;

public class ReadDBConnExecutor implements DBConnExecutorHandler {

	private Connection conn;

	@Override
	public Connection getConnection() {
		return conn;
	}

	@Override
	public Connection beginConnection(String dataSource) throws SQLException {
		conn = DBConnFactory.READ_CONNECTION(dataSource);
		return conn;
	}

	@Override
	public void commit() throws SQLException {

	}

	@Override
	public void rollback() {

	}

	@Override
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			LogUtil.error("fail when close read Connection，" + e.getMessage(), e);
		}
	}

}
