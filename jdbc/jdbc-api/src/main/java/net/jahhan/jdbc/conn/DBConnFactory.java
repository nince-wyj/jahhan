package net.jahhan.jdbc.conn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.context.BaseVariable;
import net.jahhan.jdbc.constant.enumeration.DBConnectLevel;
import net.jahhan.spi.DataSourcePoolInit;

/**
 * 
 * @author nince
 *
 */
public class DBConnFactory {

	private static AtomicLong getCONN = new AtomicLong(0L);
	private static AtomicLong freeCONN = new AtomicLong(0L);

	private static DataSourcePool dsPool = (DataSourcePool) BaseContext.CTX.getInjector().getInstance(DataSourcePoolInit.class);

	public static String getDefaultDataSource() {
		return dsPool.getDefaultDataSource();
	}

	public static long incGetConn() {
		return getCONN.incrementAndGet();
	}

	public static long incFreeConn() {
		return freeCONN.incrementAndGet();
	}

	public static void resetCount() {
		getCONN.set(0);
		freeCONN.set(0);
	}

	public static String getDbConnInfo() {
		String str = "db connection: get " + getCONN.get() + " free " + freeCONN.get() + "\n";
		try {
			return str + dsPool.getStatus();
		} catch (SQLException e) {
			return str;
		}
	}

	public static Connection WRITE_CONNECTION(String dataSource) throws SQLException {
		if (BaseVariable.getBaseVariable().isDbLazyCommit()) {
			return dsPool.getHoldConnection(dataSource);
		}else{
			return dsPool.getWriteConnection(dataSource);
		}
	}

	public static Connection BATCH_CONNECTION(String dataSource) throws SQLException {
		return dsPool.getBatchConnection(dataSource);
	}

	public static Connection READ_CONNECTION(String dataSource) throws SQLException {
		return dsPool.getReadConnection(dataSource);
	}

	public static Connection CONNECTION(String dataSource, DBConnectLevel type) throws SQLException {
		switch (type) {
		case BATCH:
			return dsPool.getBatchConnection(dataSource);
		case WRITE:
			return dsPool.getWriteConnection(dataSource);
		case READ:
			return dsPool.getReadConnection(dataSource);
		default:
			break;
		}
		return null;
	}

}
