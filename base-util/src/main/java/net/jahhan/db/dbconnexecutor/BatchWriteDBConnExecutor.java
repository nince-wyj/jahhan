package net.jahhan.db.dbconnexecutor;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.db.DBConnExecutorHandler;
import net.jahhan.db.conn.DBConnFactory;

public class BatchWriteDBConnExecutor implements DBConnExecutorHandler {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private Connection conn;

	@Override
	public Connection beginConnection() throws SQLException {
		conn = DBConnFactory.BATCH_CONNECTION();
		conn.setAutoCommit(false);
		conn.setSavepoint();
		return conn;
	}

	@Override
	public void commit() throws SQLException {
		conn.commit();
	}

	@Override
	public void rollback() {
		try {
			conn.rollback();
		} catch (SQLException e) {
			logger.error("error when rollback" + e.getMessage(), e);
		}
	}

	@Override
	public void close() {
		try {
			conn.close();
		} catch (Exception e) {
			logger.error("fail when close write Connection，" + e.getMessage(), e);
		}
		
	}

}
