package net.jahhan.jdbc.conn;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public abstract class DataSourceWrapper {

	protected final int type;

	protected boolean readOnly = false;

	protected DataSource dataSource;

	protected final String url;

	protected abstract DataSource createDataPool(PoolConfig conf);

	public DataSourceWrapper(PoolConfig conf, boolean readOnly, int type) {
		super();
		url = conf.jdbcUrl;
		this.dataSource = this.createDataPool(conf);
		this.readOnly = readOnly;
		this.type = type;
	}

	public Connection getConnection() throws SQLException {
		Connection dbConn = this.dataSource.getConnection();
		dbConn = new ConnectionWarpper(dbConn, type);
		dbConn.setAutoCommit(false);
		if (this.readOnly) {
			dbConn.setReadOnly(true);
		}
		DBConnFactory.incGetConn();
		return dbConn;
	}

	public abstract void close() throws SQLException;

	public abstract String getStatus() throws SQLException;

	public abstract void reset();
}
