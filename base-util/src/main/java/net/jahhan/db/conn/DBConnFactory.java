package net.jahhan.db.conn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author nince
 *
 */
public class DBConnFactory {
	private static Logger logger = LoggerFactory.getLogger(DBConnFactory.class);
	
	private static AtomicLong getCONN = new AtomicLong(0L);
	private static AtomicLong freeCONN = new AtomicLong(0L);

	private static DataSourcePool dsPool;
	static {
		dsPool = new DataSourcePool();
		dsPool.init();
	}

	static long incGetConn() {
		return getCONN.incrementAndGet();
	}

	static long incFreeConn() {
		return freeCONN.incrementAndGet();
	}

	public static void init() {

	}

	public static void resetCount() {
		getCONN.set(0);
		freeCONN.set(0);
	}

	public static String getDbConnInfo() {
		String str = "db connection: get " + getCONN.get() + " free "
				+ freeCONN.get() + "\n";
		try {
			return str + dsPool.getStatus();
		} catch (SQLException e) {
			return str;
		}
	}

	/**
	 * Apply Connection
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Connection CONNECTION() throws SQLException {
		return dsPool.getMainConnection();
	}
	
	public static Connection BATCH_CONNECTION() throws SQLException {
		return dsPool.getBatchConnection();
	}

	/**
	 * Apply Connection
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Connection READ_CONNECTION() throws SQLException {
		return dsPool.getReadConnection();
	}

//	/**
//	 * 历史库的连接
//	 * 
//	 * @return
//	 * @throws SQLException
//	 * 
//	 * @author nince
//	 */
//	public static Connection HIS_CONNECTION() throws SQLException {
//		return dsPool.getHisConnection();
//	}

	/**
	 * Free Connection
	 * 
	 * @param dbConn
	 * @throws SQLException
	 */
	public static void freeConnection(Connection dbConn) throws SQLException {
		DataSourcePool.freeConnection(dbConn);
	}

	/**
	 * free Connections
	 * 
	 * @param conns
	 */
	public static void freeConns(Iterable<Connection> conns) {
		if (conns != null) {
			try {
				for (Connection conn : conns) {
					try {
						logger.error("存在未关闭的连接,尝试全局连接...");
						DBConnFactory.freeConnection(conn);
					} catch (Exception e) {
						logger.error("尝试全局关闭连接失败。", e);
					}
				}
			} catch (Exception e) {
			}
		}
	}
}
