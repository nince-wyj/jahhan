package net.jahhan.db.dbconnexecutor;

import java.sql.SQLException;

import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.db.DBConnExecutorHandler;

public class DBConnExecutorFactory {

	private DBConnectionType dbType = null;

	private DBConnExecutorHandler dBConnExecutor = null;

	public DBConnExecutorFactory(DBConnectionType dbType) {
		this.dbType = dbType;
	}

	public void beginConnection() throws SQLException {
		switch (dbType) {
		case WEAK_WRITE:
			dBConnExecutor = new WeakWriteDBConnExecutor();
			break;
		case DISTRIBUTED_WEAK_WRITE:
			dBConnExecutor = new WeakWriteDBConnExecutor();
			break;
		case BATCH_WRITE:
			dBConnExecutor = new BatchWriteDBConnExecutor();
			break;
		case READ:
			dBConnExecutor = new ReadDBConnExecutor();
			break;
		default:
			dBConnExecutor = new NoneDBConnExecutor();
		}
		dBConnExecutor.beginConnection();
	}

	public void endConnection() throws SQLException {

		switch (dbType) {
		case WEAK_WRITE: {
			if (dBConnExecutor != null) {
				dBConnExecutor.commit();
			}
			break;
		}
		case DISTRIBUTED_WEAK_WRITE: {
			if (dBConnExecutor != null) {
				dBConnExecutor.commit();
			}
			break;
		}
		case BATCH_WRITE: {
			if (dBConnExecutor != null) {
				dBConnExecutor.commit();
			}
			break;
		}
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
		}
	}
}
