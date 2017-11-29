package net.jahhan.jdbc.conn;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.jdbc.conn.DataSourceWrapper;
import net.jahhan.jdbc.conn.PoolConfig;

@Slf4j
public class C3p0DataSourceWrapper extends DataSourceWrapper {

	public C3p0DataSourceWrapper(PoolConfig conf, boolean readOnly, int type) {
		super(conf, readOnly, type);
	}

	protected DataSource createDataPool(PoolConfig conf) {
		if (!conf.isValid()) {
			throw new RuntimeException("数据库驱动为空或者数据库用户名或者密码或者连接字符串为空");
		}
		log.info("db config:{}", conf.toString());
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		cpds.setJdbcUrl(conf.jdbcUrl);
		cpds.setUser(conf.userName);
		cpds.setPassword(conf.password);
		try {
			cpds.setDriverClass(conf.driverClass);
		} catch (PropertyVetoException e) {
			throw new RuntimeException("数据库驱动设置失败");
		}
		cpds.setPreferredTestQuery("select 1");
		cpds.setIdleConnectionTestPeriod(60);
		if (conf.initialPoolSize != null && !conf.initialPoolSize.isEmpty()) {
			cpds.setInitialPoolSize(Integer.parseInt(conf.initialPoolSize));
		}
		if (conf.acquireIncrement != null && !conf.acquireIncrement.isEmpty()) {
			cpds.setAcquireIncrement(Integer.parseInt(conf.acquireIncrement));
		}
		if (conf.maxPoolSize != null && !conf.maxPoolSize.isEmpty()) {
			cpds.setMaxPoolSize(Integer.parseInt(conf.maxPoolSize));
		}
		if (conf.minPoolSize != null && !conf.minPoolSize.isEmpty()) {
			cpds.setMinPoolSize(Integer.parseInt(conf.minPoolSize));
		}

		if (conf.checkoutTimeout != null && !conf.checkoutTimeout.isEmpty()) {
			cpds.setCheckoutTimeout(Integer.parseInt(conf.checkoutTimeout));
		}
		if (conf.maxStatements != null && !conf.maxStatements.isEmpty()) {
			cpds.setMaxStatements(Integer.parseInt(conf.maxStatements));
		}
		if (conf.maxStatementsPerConnection != null && !conf.maxStatementsPerConnection.isEmpty()) {
			cpds.setMaxStatementsPerConnection(Integer.parseInt(conf.maxStatementsPerConnection));
		}
		if (conf.maxIdleTime != null && !conf.maxIdleTime.isEmpty()) {
			cpds.setMaxIdleTime(Integer.parseInt(conf.maxIdleTime));
		}
		if (conf.unreturnedConnectionTimeout != null && !conf.unreturnedConnectionTimeout.isEmpty()) {
			cpds.setDebugUnreturnedConnectionStackTraces(true);
			cpds.setUnreturnedConnectionTimeout(Integer.parseInt(conf.unreturnedConnectionTimeout));
		} else {
			cpds.setDebugUnreturnedConnectionStackTraces(false);
		}
		return cpds;
	}

	public void close() throws SQLException {
		((ComboPooledDataSource) this.dataSource).close();
	}

	public String getStatus() throws SQLException {
		StringBuilder sb = new StringBuilder(20);
		sb.append(url);
		sb.append("\nbusyConnection:" + ((ComboPooledDataSource) this.dataSource).getNumBusyConnectionsDefaultUser());
		sb.append("\nidleConnection:" + ((ComboPooledDataSource) this.dataSource).getNumIdleConnectionsDefaultUser());
		sb.append("\nEffectivePropertyCycle:"
				+ ((ComboPooledDataSource) this.dataSource).getEffectivePropertyCycleDefaultUser());
		sb.append("\nLastIdleTestFailure:" + ((ComboPooledDataSource) this.dataSource).getLastIdleTestFailureDefaultUser());
		sb.append("\nNumFailedCheckins:" + ((ComboPooledDataSource) this.dataSource).getNumFailedCheckinsDefaultUser());
		sb.append("\nNumFailedCheckouts:" + ((ComboPooledDataSource) this.dataSource).getNumFailedCheckoutsDefaultUser());
		sb.append("\nNumFailedIdleTests:" + ((ComboPooledDataSource) this.dataSource).getNumFailedIdleTestsDefaultUser());
		sb.append("\nNumThreadsAwaitingCheckout:"
				+ ((ComboPooledDataSource) this.dataSource).getNumThreadsAwaitingCheckoutDefaultUser());
		sb.append(
				"\nThreadPoolNumActiveThreads:" + ((ComboPooledDataSource) this.dataSource).getThreadPoolNumActiveThreads());
		sb.append("\nThreadPoolNumIdleThreads:" + ((ComboPooledDataSource) this.dataSource).getThreadPoolNumIdleThreads());
		sb.append("\nThreadPoolNumTasksPending:" + ((ComboPooledDataSource) this.dataSource).getThreadPoolNumTasksPending());
		sb.append("\nLastAcquisitionFailure:"
				+ ((ComboPooledDataSource) this.dataSource).getLastAcquisitionFailureDefaultUser());
		sb.append("\nLastCheckoutFailure:" + ((ComboPooledDataSource) this.dataSource).getLastCheckoutFailureDefaultUser());
		sb.append("\nLastCheckinFailure:" + ((ComboPooledDataSource) this.dataSource).getLastCheckinFailureDefaultUser());
		sb.append("\nNumStatements:" + ((ComboPooledDataSource) this.dataSource).getStatementCacheNumStatementsDefaultUser());
		sb.append("\nNumConnectionsWithCachedStatements:"
				+ ((ComboPooledDataSource) this.dataSource).getStatementCacheNumConnectionsWithCachedStatementsDefaultUser());
		return sb.toString();
	}

	public void reset() {
		try {
			((ComboPooledDataSource) this.dataSource).softResetDefaultUser();
		} catch (SQLException e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}
