package net.jahhan.db.dbconnexecutor;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.db.DBConnExecutorHandler;
import net.jahhan.db.conn.DBConnFactory;

public class ReadDBConnExecutor implements DBConnExecutorHandler {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private Connection conn;

	@Override
	public Connection beginConnection() throws SQLException {
		conn = DBConnFactory.READ_CONNECTION();
		return conn;
	}

    @Override
    public void commit() throws SQLException {
    	
    }

    @Override
    public void rollback() {

    }

    @Override
    public void close() {
    	try {
			conn.close();
		} catch (SQLException e) {
			logger.error("fail when close read Connection，" + e.getMessage(), e);
		}
    }

}
