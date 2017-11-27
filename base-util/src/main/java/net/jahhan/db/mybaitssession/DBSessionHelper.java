package net.jahhan.db.mybaitssession;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.managed.ManagedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.context.AppContext;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.SessionHandler;
import net.jahhan.db.conn.ConnectionWarpper;
import net.jahhan.db.conn.DBConnFactory;
import net.jahhan.db.conn.DataSource;
import net.jahhan.exception.FrameworkException;
import net.jahhan.utils.ScanUtils;

public class DBSessionHelper implements SessionHandler {

	static Logger logger = LoggerFactory.getLogger(DBSessionHelper.class);

	private static DBSessionHelper sessionManager = null;

	private static Configuration configuration;

	static {
		try {
			AppContext.setSessionManager(DBSessionHelper.instance());
			ClassLoader cl = DBSessionHelper.class.getClassLoader();
			configuration = new Configuration();
			Set<String> xmlPaths = ScanUtils.findResourceByPathRule(".+\\.xml", "mapper/", "mapper2/");
			for (String xmlPath : xmlPaths) {
				try (InputStream is = cl.getResourceAsStream(xmlPath)) {
					XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(is, configuration, xmlPath,
							configuration.getSqlFragments());
					xmlMapperBuilder.parse();
				} catch (Exception e) {
					logger.error(xmlPath + "文件错误," + e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			logger.error("mybatis配置文件错误," + e.getMessage(), e);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				logger.error(e.getMessage(), e);
			}
			System.exit(-1);
		}
	}

	private DBSessionHelper() {

	}

	public static DBSessionHelper instance() {
		if (null == sessionManager) {
			sessionManager = new DBSessionHelper();
		}
		return sessionManager;
	}

	public SqlSession getMainReadSession() {
		try {
			return getSqlSession(getMainReadConnection());
		} catch (SQLException e) {
			logger.error(e.getMessage());
			FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "数据库错误");
			return null;
		}
	}

	@Override
	public SqlSession getMainSession() {
		try {
			return getSqlSession(getMainConnection());
		} catch (SQLException e) {
			FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "数据库错误");
			return null;
		}
	}

	@Override
	public SqlSession getBatchSession() {
		try {
			return getBatchSqlSession(getBatchConnection());
		} catch (SQLException e) {
			FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "数据库错误");
			return null;
		}
	}

	public SqlSession getSqlSession(Connection conn) throws SQLException {
		ManagedTransaction transaction = new ManagedTransaction(conn, false);
		SimpleExecutor excutor = new SimpleExecutor(configuration, transaction);
		return new DefaultSqlSessionHelper(configuration, excutor);
	}

	public SqlSession getBatchSqlSession(Connection conn) throws SQLException {
		ManagedTransaction transaction = new ManagedTransaction(conn, false);
		BatchExecutor excutor = new BatchExecutor(configuration, transaction);
		return new DefaultSqlSessionHelper(configuration, excutor);
	}

	public void setConn(Connection conn) {
		InvocationContext ic = BaseContext.CTX.getInvocationContext();
		ic.setCurrentConn(conn);
	}

	private Connection getMainConnection() throws SQLException {
		Connection cur = _getConnection(DataSource.MAIN_WRITE);
		if (cur == null) {
			FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "数据库错误");
		}
		return cur;
	}

	private Connection getBatchConnection() throws SQLException {
		Connection cur = _getConnection(DataSource.BATCH);
		if (cur == null) {
			FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "数据库错误");
		}
		return cur;
	}

	private Connection getMainReadConnection() throws SQLException {
		Connection cur = _getConnection(DataSource.MAIN_READ);
		if (cur == null) {
			return DBConnFactory.READ_CONNECTION();
		}
		return cur;
	}

	/**
	 * 获取线程中的connection。不存在相同类型，就返回null. 写永远都是取最近的，读可选是否使用写连接
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection _getConnection(int type) throws SQLException {
		InvocationContext ic = BaseContext.CTX.getInvocationContext();
		if (ic == null) {
			return null;
		}
		if (type == DataSource.MAIN_READ) {
			Connection cur = ic.getCurrentConn();
			// 从当前连接中获取最后一个与需求类型一样的连接
			if (cur != null && ic.getConnectionType().equals(DBConnectionType.READ)) {
				return cur;
			}
		}
		List<Connection> conns = ic.getConnections();
		for (int i = conns.size() - 1; i > -1; i--) {
			Connection conn = conns.get(i);
			if (!ConnectionWarpper.class.isInstance(conn)) {
				continue;
			}
			ConnectionWarpper warp = (ConnectionWarpper) conn;
			if (warp.getType() == type && (!ic.getConnectionType().equals(DBConnectionType.WEAK_WRITE)
					&& !ic.getConnectionType().equals(DBConnectionType.DISTRIBUTED_WEAK_WRITE)
					&& !ic.getConnectionType().equals(DBConnectionType.BATCH_WRITE) || type == DataSource.MAIN_WRITE)) {
				return warp;
			}
			if (warp.getType() == type && (!ic.getConnectionType().equals(DBConnectionType.WEAK_WRITE)
					&& !ic.getConnectionType().equals(DBConnectionType.DISTRIBUTED_WEAK_WRITE)
					&& !ic.getConnectionType().equals(DBConnectionType.BATCH_WRITE) || type == DataSource.BATCH)) {
				return warp;
			}
			if (type == DataSource.MAIN_WRITE && ic.getConnectionType().equals(DBConnectionType.BATCH_WRITE)
					&& warp.getType() == DataSource.BATCH) {
				return warp;
			}
			if (type == DataSource.MAIN_READ && ic.getConnectionType().equals(DBConnectionType.WEAK_WRITE)
					&& warp.getType() == DataSource.MAIN_WRITE) {
				return warp;
			}
			if (type == DataSource.MAIN_WRITE && ic.getConnectionType().equals(DBConnectionType.DISTRIBUTED_WEAK_WRITE)
					&& warp.getType() == DataSource.MAIN_WRITE) {
				return warp;
			}
		}
		return null;

	}

	static void init() {

	}

	// @Override
	// public SqlSession getHisSession() {
	// try {
	// return getSqlSession(DBConnFactory.HIS_CONNECTION());
	// } catch (SQLException e) {
	// SystemException.throwException(SystemErrorCode.DATABASE_ERROR, "数据库错误");
	// return null;
	// }
	// }

	// @Override
	// public SqlSession getFlowSession() {
	// return null;
	// }
	//
	// @Override
	// public SqlSession getFlowReadSession() {
	// return null;
	// }

	@Override
	public Connection getConnection() throws SQLException {
		return DBConnFactory.CONNECTION();
	}

}
