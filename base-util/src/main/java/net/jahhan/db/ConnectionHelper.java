package net.jahhan.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.conn.DBConnFactory;
import net.jahhan.db.conn.DataSource;
import net.jahhan.db.mybaitssession.DBSessionHelper;
import net.jahhan.db.publish.DBPublisherHandler;

public class ConnectionHelper {

    static Logger logger = LoggerFactory.getLogger(ConnectionHelper.class);

    private static DBSessionHelper sessionManager=DBSessionHelper.instance();
	public static void init(){
	}
	static{
		DBPublisherHandler.init();
	}
//    public static Connection CONNECTION() throws SQLException {
//        Connection conn = DBConnFactory.CONNECTION();
//        setConnection(conn);
//        return conn;
//    }
//
//    private static void setConnection(Connection conn) {
//        if (conn != null) {
//            InvocationContext ic = ApplicationContext.CTX.getInvocationContext();
//            Connection cur = ic.getCurrentConn();
//            if (cur != null) {
//                logger.error("已经存在事务连接，不能重复设置！！");
//                SystemException.throwException(-1, "已经存在事务连接，不能重复设置！！");
//            }
//        }
//        sessionManager.setConn(conn);
//    }

    /**
     * 清除事务所用的连接，但不关闭连接
     */
    public static void clearTransaction() {
        sessionManager.setConn(null);
    }

    /**
     * @return true表示是事务型连接，即读写用同一个连接.
     * @throws SQLException
     */
    private static boolean closeTransact() throws SQLException {
        InvocationContext ic = ApplicationContext.CTX.getInvocationContext();
        Connection cur = ic.getCurrentConn();
        if (cur != null) {
            DBConnFactory.freeConnection(cur);
            return true;
        }
        return false;
    }

    /**
     * 关闭连接
     */
    public static void closeConnection() {
        try {
            if (closeTransact()) {
                return;
            }
            DBConnFactory.freeConnection(sessionManager._getConnection(DataSource.MAIN_READ));
            DBConnFactory.freeConnection(sessionManager._getConnection(DataSource.MAIN_WRITE));
            DBConnFactory.freeConnection(sessionManager._getConnection(DataSource.BATCH));
            DBConnFactory.freeConnection(sessionManager._getConnection(DataSource.MAIN_READ));
        } catch (SQLException e) {
            logger.error("关闭连接失败，" + e.getMessage());
        }
    }

    /**
     * 仅关闭读连接
     */
    public static void closeReadConnection() {
        try {
            DBConnFactory.freeConnection(sessionManager._getConnection(DataSource.MAIN_READ));
        } catch (SQLException e) {
            logger.error("关闭连接失败，" + e.getMessage());
        }
    }

    public static void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e1) {
            logger.error(e1.getMessage(), e1);
        }
    }

}
