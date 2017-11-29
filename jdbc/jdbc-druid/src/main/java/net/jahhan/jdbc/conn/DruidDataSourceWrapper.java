package net.jahhan.jdbc.conn;

import java.sql.SQLException;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.jdbc.conn.DataSourceWrapper;
import net.jahhan.jdbc.conn.PoolConfig;

@Slf4j
public class DruidDataSourceWrapper extends DataSourceWrapper {

	public DruidDataSourceWrapper(PoolConfig conf, boolean readOnly, int type) {
		super(conf, readOnly, type);
	}

	protected DataSource createDataPool(PoolConfig conf) {
		if (!conf.isValid()) {
			throw new RuntimeException("数据库驱动为空或者数据库用户名或者密码或者连接字符串为空");
		}
		log.info("db config:{}", conf.toString());
		com.alibaba.druid.pool.DruidDataSource cpds = new com.alibaba.druid.pool.DruidDataSource();
		cpds.setUrl(conf.jdbcUrl);
		cpds.setUsername(conf.userName);
		cpds.setPassword(conf.password);
		cpds.setDriverClassName(conf.driverClass);
		cpds.setValidationQueryTimeout(60);
		try {
			cpds.setFilters("stat");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (conf.initialPoolSize != null && !conf.initialPoolSize.isEmpty()) {
			cpds.setInitialSize(Integer.parseInt(conf.initialPoolSize));
		}
		// if (conf.acquireIncrement != null &&
		// !conf.acquireIncrement.isEmpty()) {
		// cpds.setAcquireIncrement(Integer.parseInt(conf.acquireIncrement));
		// }
		if (conf.maxPoolSize != null && !conf.maxPoolSize.isEmpty()) {
			cpds.setMaxActive(Integer.parseInt(conf.maxPoolSize));
		}
		if (conf.minPoolSize != null && !conf.minPoolSize.isEmpty()) {
			cpds.setMinIdle(Integer.parseInt(conf.minPoolSize));
		}

		// if (conf.checkoutTimeout != null && !conf.checkoutTimeout.isEmpty())
		// {
		// cpds.setCheckoutTimeout(Integer.parseInt(conf.checkoutTimeout));
		// }
		// if (conf.maxStatements != null && !conf.maxStatements.isEmpty()) {
		// cpds.setMaxStatements(Integer.parseInt(conf.maxStatements));
		// }
		// if (conf.maxStatementsPerConnection != null &&
		// !conf.maxStatementsPerConnection.isEmpty()) {
		// cpds.setMaxStatementsPerConnection(Integer.parseInt(conf.maxStatementsPerConnection));
		// }
		// if (conf.maxIdleTime != null && !conf.maxIdleTime.isEmpty()) {
		// cpds.setMaxIdleTime(Integer.parseInt(conf.maxIdleTime));
		// }
		if (conf.unreturnedConnectionTimeout != null && !conf.unreturnedConnectionTimeout.isEmpty()) {
			cpds.setLogAbandoned(true);
			cpds.setRemoveAbandonedTimeout(Integer.parseInt(conf.unreturnedConnectionTimeout));
		} else {
			cpds.setLogAbandoned(false);
		}
		return cpds;
	}

	public void close() throws SQLException {
		((DruidDataSourceWrapper) this.dataSource).close();
	}

	public String getStatus() throws SQLException {
		StringBuilder sb = new StringBuilder(20);
		sb.append(url);
		// sb.append("\nbusyConnection:" + ((DruidDataSource)
		// this.cpds).getNumBusyConnectionsDefaultUser());
		// sb.append("\nidleConnection:" + ((DruidDataSource)
		// this.cpds).getNumIdleConnectionsDefaultUser());
		// sb.append("\nEffectivePropertyCycle:"
		// + ((DruidDataSource)
		// this.cpds).getEffectivePropertyCycleDefaultUser());
		// sb.append("\nLastIdleTestFailure:" + ((DruidDataSource)
		// this.cpds).getLastIdleTestFailureDefaultUser());
		// sb.append("\nNumFailedCheckins:" + ((DruidDataSource)
		// this.cpds).getNumFailedCheckinsDefaultUser());
		// sb.append("\nNumFailedCheckouts:" + ((DruidDataSource)
		// this.cpds).getNumFailedCheckoutsDefaultUser());
		// sb.append("\nNumFailedIdleTests:" + ((DruidDataSource)
		// this.cpds).getNumFailedIdleTestsDefaultUser());
		// sb.append("\nNumThreadsAwaitingCheckout:"
		// + ((DruidDataSource)
		// this.cpds).getNumThreadsAwaitingCheckoutDefaultUser());
		// sb.append(
		// "\nThreadPoolNumActiveThreads:" + ((DruidDataSource)
		// this.cpds).getThreadPoolNumActiveThreads());
		// sb.append("\nThreadPoolNumIdleThreads:" + ((DruidDataSource)
		// this.cpds).getThreadPoolNumIdleThreads());
		// sb.append("\nThreadPoolNumTasksPending:" + ((DruidDataSource)
		// this.cpds).getThreadPoolNumTasksPending());
		// sb.append("\nLastAcquisitionFailure:"
		// + ((DruidDataSource)
		// this.cpds).getLastAcquisitionFailureDefaultUser());
		// sb.append("\nLastCheckoutFailure:" + ((DruidDataSource)
		// this.cpds).getLastCheckoutFailureDefaultUser());
		// sb.append("\nLastCheckinFailure:" + ((DruidDataSource)
		// this.cpds).getLastCheckinFailureDefaultUser());
		// sb.append("\nNumStatements:" + ((DruidDataSource)
		// this.cpds).getStatementCacheNumStatementsDefaultUser());
		// sb.append("\nNumConnectionsWithCachedStatements:"
		// + ((DruidDataSource)
		// this.cpds).getStatementCacheNumConnectionsWithCachedStatementsDefaultUser());
		return sb.toString();
	}

	public void reset() {
		((com.alibaba.druid.pool.DruidDataSource) this.dataSource).resetStat();
	}
}
