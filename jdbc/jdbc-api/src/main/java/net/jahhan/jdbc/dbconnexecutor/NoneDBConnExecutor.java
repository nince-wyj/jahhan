package net.jahhan.jdbc.dbconnexecutor;

import java.sql.Connection;
import java.sql.SQLException;

import net.jahhan.jdbc.DBConnExecutorHandler;

public class NoneDBConnExecutor implements DBConnExecutorHandler {

	@Override
	public Connection getConnection() {
		return null;
	}

	@Override
	public Connection beginConnection(String dataSource) {
		return null;
	}

	@Override
	public void commit() throws SQLException {

	}

	@Override
	public void rollback() {
	}

	@Override
	public void close() {

	}

}
