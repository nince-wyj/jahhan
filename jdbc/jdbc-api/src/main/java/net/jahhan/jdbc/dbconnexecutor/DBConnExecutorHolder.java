package net.jahhan.jdbc.dbconnexecutor;

import java.sql.SQLException;

import lombok.Getter;
import net.jahhan.jdbc.DBConnExecutorHandler;
import net.jahhan.jdbc.constant.enumeration.DBConnectLevel;
import net.jahhan.jdbc.context.DBVariable;

public class DBConnExecutorHolder {
	private String dataSource;
	@Getter
	private DBConnExecutorHandler dBConnExecutor = null;
	@Getter
	private DBConnectLevel dbConnectLevel;
	@Getter
	private long startTime;

	public DBConnExecutorHolder(String dataSource, DBConnectLevel dbConnectLevel) {
		this.dataSource = dataSource;
		this.dbConnectLevel = dbConnectLevel;
		this.startTime = System.currentTimeMillis();
	}

	public void beginConnection() throws SQLException {
		switch (dbConnectLevel) {
		case BATCH:
			dBConnExecutor = new BatchWriteDBConnExecutor();
			break;
		case WRITE:
			dBConnExecutor = new TransactionWriteDBConnExecutor();
			break;
		case READ:
			dBConnExecutor = new ReadDBConnExecutor();
			break;
		default:
			dBConnExecutor = new NoneDBConnExecutor();
		}
		dBConnExecutor.beginConnection(dataSource);
	}

	public void commit() throws SQLException {
		switch (dbConnectLevel) {
		case BATCH:
			if (dBConnExecutor != null) {
				dBConnExecutor.commit();
			}
			break;
		case WRITE:
			if (dBConnExecutor != null) {
				dBConnExecutor.commit();
			}
			break;
		default:
		}
	}

	public void rollback() {
		if (dBConnExecutor != null) {
			dBConnExecutor.rollback();
		}
	}

	public void close() {
		if (dBConnExecutor != null) {
			dBConnExecutor.close();
			DBVariable dbVariable = DBVariable.getDBVariable();
			dbVariable.removeDBConnExecutorHolder(dataSource, this);
		}
	}
}
