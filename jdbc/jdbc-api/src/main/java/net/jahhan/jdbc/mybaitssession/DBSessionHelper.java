package net.jahhan.jdbc.mybaitssession;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.transaction.managed.ManagedTransaction;

import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.jdbc.DBConnExecutorHandler;
import net.jahhan.jdbc.SessionHandler;
import net.jahhan.jdbc.annotation.DBConnect;
import net.jahhan.jdbc.conn.ConnectionWarpper;
import net.jahhan.jdbc.conn.DBConnFactory;
import net.jahhan.jdbc.constant.enumeration.DBConnectLevel;
import net.jahhan.jdbc.constant.enumeration.DBConnectStrategy;
import net.jahhan.jdbc.dbconnexecutor.DBConnExecutorHolder;
import net.jahhan.variable.DBVariable;

@Singleton
public class DBSessionHelper implements SessionHandler {

	private static Configuration configuration;

	static {
		try {
			ClassLoader cl = DBSessionHelper.class.getClassLoader();
			configuration = new Configuration();
			Set<String> xmlPaths = ClassScaner.findResourceByPathRule(".+\\.xml", "mapper/", "mapper2/");
			for (String xmlPath : xmlPaths) {
				try (InputStream is = cl.getResourceAsStream(xmlPath)) {
					XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(is, configuration, xmlPath,
							configuration.getSqlFragments());
					xmlMapperBuilder.parse();
				} catch (Exception e) {
					LogUtil.error(xmlPath + "文件错误," + e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			LogUtil.error("mybatis配置文件错误," + e.getMessage(), e);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				LogUtil.error(e.getMessage(), e);
			}
			System.exit(-1);
		}
	}

	public SqlSession getReadSession(String dataSource) {
		try {
			return getSqlSession(getReadConnection(dataSource));
		} catch (SQLException e) {
			JahhanException.throwException(JahhanErrorCode.DATABASE_ERROR, "数据库错误", e);
			return null;
		}
	}

	@Override
	public SqlSession getWriteSession(String dataSource) {
		try {
			return getSqlSession(getWriteConnection(dataSource));
		} catch (SQLException e) {
			JahhanException.throwException(JahhanErrorCode.DATABASE_ERROR, "数据库错误", e);
			return null;
		}
	}

	@Override
	public SqlSession getBatchSession(String dataSource) {
		try {
			return getSqlSession(getBatchConnection(dataSource));
		} catch (SQLException e) {
			JahhanException.throwException(JahhanErrorCode.DATABASE_ERROR, "数据库错误", e);
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

	private Connection getWriteConnection(String dataSource) throws SQLException {
		Connection cur = _getConnection(dataSource, DBConnectLevel.WRITE);
		if (cur == null) {
			JahhanException.throwException(JahhanErrorCode.DATABASE_ERROR, "数据库错误");
		}
		return cur;
	}

	private Connection getBatchConnection(String dataSource) throws SQLException {
		Connection cur = _getConnection(dataSource, DBConnectLevel.BATCH);
		if (cur == null) {
			JahhanException.throwException(JahhanErrorCode.DATABASE_ERROR, "数据库错误");
		}
		return cur;
	}

	private Connection getReadConnection(String dataSource) throws SQLException {
		Connection cur = _getConnection(dataSource, DBConnectLevel.READ);
		if (cur == null) {
			return DBConnFactory.READ_CONNECTION(dataSource);
		}
		return cur;
	}

	public Connection _getConnection(String dataSource, DBConnectLevel type) throws SQLException {
		DBVariable dbVariable = (DBVariable) DBVariable.getThreadVariable("db");
		if (dbVariable == null) {
			return null;
		}
		List<DBConnExecutorHolder> dbConnExecutorHolders = dbVariable.getDBConnExecutorHolders(dataSource);
		DBConnectStrategy dbConnectStrategy = dbVariable.getDBConnectStrategy(dataSource);
		if (null == dbConnectStrategy) {
			dbVariable.initConnectionData(dataSource);
			dbConnectStrategy = DBConnect.defaultDBConnectStrategy;
			dbVariable.setDBConnectStrategy(dataSource, dbConnectStrategy);

		}
		DBConnectLevel dbConnectLevel = dbVariable.getDbConnectLevel(dataSource);
		switch (dbConnectStrategy) {
		case UPDATA: {
			if (type.getLevel() <= dbConnectLevel.getLevel()) {
				DBConnExecutorHolder currentDBConnExecutorHolder = dbVariable
						.getCurrentDBConnExecutorHolder(dataSource);
				if (null != currentDBConnExecutorHolder) {
					DBConnExecutorHandler dbConnExecutor = currentDBConnExecutorHolder.getDBConnExecutor();
					if (null != dbConnExecutor) {
						Connection currentConn = dbConnExecutor.getConnection();
						if (currentConn != null) {
							return currentConn;
						}
					}
				}
			}
			dbVariable.initConnectionData(dataSource);
			DBConnExecutorHolder connExec = new DBConnExecutorHolder(dataSource, type);
			connExec.beginConnection();
			dbVariable.setConnectionLevel(dataSource, type);
			dbVariable.addDBConnExecutorHolder(dataSource, connExec);
			dbVariable.setCurrentDBConnExecutorHolder(dataSource, connExec);
			return connExec.getDBConnExecutor().getConnection();
		}
		case ORIGINAL: {
			for (int i = dbConnExecutorHolders.size() - 1; i > -1; i--) {
				DBConnExecutorHolder dbConnExecutorHolder = dbConnExecutorHolders.get(i);
				Connection conn = dbConnExecutorHolder.getDBConnExecutor().getConnection();
				if (!ConnectionWarpper.class.isInstance(conn)) {
					continue;
				}
				ConnectionWarpper warp = (ConnectionWarpper) conn;
				if (warp.getType() == type.getLevel()) {
					return warp;
				}
			}
			dbVariable.initConnectionData(dataSource);
			DBConnExecutorHolder connExec = new DBConnExecutorHolder(dataSource, type);
			connExec.beginConnection();
			dbVariable.addDBConnExecutorHolder(dataSource, connExec);
			dbVariable.setCurrentDBConnExecutorHolder(dataSource, connExec);
			return connExec.getDBConnExecutor().getConnection();
		}
		default:
			break;
		}
		return null;

	}

	@Override
	public Connection getConnection(String dataSource) throws SQLException {
		return DBConnFactory.WRITE_CONNECTION(dataSource);
	}

}
