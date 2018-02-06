package net.jahhan.jdbc.dbconnexecutor;

import java.sql.Connection;
import java.sql.SQLException;

import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.jdbc.DBConnExecutorHandler;
import net.jahhan.jdbc.conn.DBConnFactory;

public class TransactionWriteDBConnExecutor implements DBConnExecutorHandler {

	private Connection conn;

	@Override
	public Connection getConnection() {
		return conn;
	}

	@Override
	public Connection beginConnection(String dataSource) throws SQLException {
		conn = DBConnFactory.WRITE_CONNECTION(dataSource);
		conn.setAutoCommit(false);
		conn.setSavepoint();
		return conn;
	}

	@Override
	public void commit() throws SQLException {
		conn.commit();
		conn.setAutoCommit(true);
	}

	@Override
	public void rollback() {
		try {
			conn.rollback();
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			LogUtil.error("error when rollback" + e.getMessage(), e);
		}
	}

	@Override
	public void close() {
		try {
			conn.close();
		} catch (Exception e) {
			LogUtil.error("fail when close write Connection，" + e.getMessage(), e);
		}

	}

}
