package net.jahhan.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据事务操作
 * 
 * @author nince
 */
public interface DBConnExecutorHandler {
    Connection beginConnection() throws SQLException;

    void commit() throws SQLException;

    void rollback();

    void close();
}
