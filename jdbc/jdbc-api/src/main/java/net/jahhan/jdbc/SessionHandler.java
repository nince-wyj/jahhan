package net.jahhan.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.session.SqlSession;

public interface SessionHandler {
	/**
	 * 获取一个全新的主库连接，与当前连接不在一个事务里面
	 * 
	 * @return
	 * @author nince
	 * @throws SQLException
	 */
	Connection getConnection(String dataSource) throws SQLException;

	SqlSession getWriteSession(String dataSource);

	SqlSession getBatchSession(String dataSource);

	SqlSession getReadSession(String dataSource);
}