package net.jahhan.db;

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
    Connection getConnection() throws SQLException;

    SqlSession getMainSession();
    
    SqlSession getBatchSession();

    SqlSession getMainReadSession();

//    /**
//     * 历史数据库的连接
//     * 
//     * @return
//     * @author nince
//     */
//    SqlSession getHisSession();
//
//    SqlSession getFlowSession();
//
//    SqlSession getFlowReadSession();
}