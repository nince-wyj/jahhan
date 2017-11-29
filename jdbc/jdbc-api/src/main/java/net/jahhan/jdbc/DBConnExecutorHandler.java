package net.jahhan.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据连接
 * 
 * @author nince
 */
public interface DBConnExecutorHandler {
    public Connection beginConnection(String dataSource) throws SQLException;

    public Connection getConnection();
    
    public void commit() throws SQLException;

    public void rollback();

    public void close();
}
