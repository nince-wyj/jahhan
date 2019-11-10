package net.jahhan.jdbc.conn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.jahhan.jdbc.constant.JDBCConstants;

@Slf4j
public class DataSourceConnectionPool {
	@Getter
	@Setter
	private DataSourceWrapper writeDS;
	@Getter
	@Setter
	private DataSourceWrapper holdDS;
	@Getter
	@Setter
	private DataSourceWrapper batchDS;
	@Getter
	@Setter
	private List<DataSourceWrapper> readDSList;
	@Getter
	@Setter
	private String dataSource;

	private AtomicInteger ai = new AtomicInteger();

	public Connection getBatchConnection() throws SQLException {
		log.debug("开启批写库连接");
		return batchDS.getConnection();
	}

	public Connection getWriteConnection() throws SQLException {
		log.debug("开启写库连接");
		return writeDS.getConnection();
	}

	public Connection getHoldConnection() throws SQLException {
		log.debug("开启长写库连接");
		return holdDS.getConnection();
	}

	public Connection getReadConnection() throws SQLException {
		int index = getReadConnIdx();
		// int index = readCount % readDSList.size();
		log.debug("开启读库连接index={}", index);
		return readDSList.get(index).getConnection();
	}

	public String getStatus() throws SQLException {
		Set<DataSourceWrapper> set = new HashSet<>();
		set.addAll(readDSList);
		set.add(writeDS);
		set.add(holdDS);
		set.add(batchDS);
		StringBuilder sb = new StringBuilder();
		for (DataSourceWrapper ds : set) {
			sb.append(ds.getStatus() + "\n");
		}
		return sb.toString();
	}

	public int getReadConnIdx() {
//		int i = ai.getAndIncrement();
//		ai.compareAndSet(readDSList.size(), 0);
//		return i;
		return ai.getAndUpdate(new IntUnaryOperator() {

			@Override
			public int applyAsInt(int operand) {
				return (operand + 1) % readDSList.size();
			}
		});
	}

	public PoolConfig createBatchConf(Properties property) {
		PoolConfig conf = new PoolConfig();
		conf.driverClass = property.getProperty("jdbc.driver");

		conf.jdbcUrl = property.getProperty(dataSource + ".write.jdbc.url");
		conf.userName = property.getProperty(dataSource + ".write.jdbc.username");
		conf.password = property.getProperty(dataSource + ".write.jdbc.password");

		conf.initialPoolSize = property.getProperty(dataSource + ".batch.jdbc.initialPoolSize");
		conf.acquireIncrement = property.getProperty(dataSource + ".batch.jdbc.acquireIncrement");
		conf.maxPoolSize = property.getProperty(dataSource + ".batch.jdbc.poolsizemax");
		conf.minPoolSize = property.getProperty(dataSource + ".batch.jdbc.poolsizemin");

		conf.checkoutTimeout = property.getProperty(dataSource + ".batch.jdbc.checkoutTimeout");
		conf.maxStatements = property.getProperty(dataSource + ".batch.jdbc.maxStatements");
		conf.maxStatementsPerConnection = property.getProperty(dataSource + ".batch.jdbc.maxStatementsPerConnection");
		conf.maxIdleTime = property.getProperty(dataSource + ".batch.jdbc.maxIdleTime");
		conf.unreturnedConnectionTimeout = property.getProperty(dataSource + ".batch.jdbc.unreturnedConnectionTimeout");
		return conf;
	}

	public PoolConfig createHoldConf(Properties property) {
		PoolConfig conf = new PoolConfig();
		conf.driverClass = property.getProperty("jdbc.driver");

		conf.jdbcUrl = property.getProperty(dataSource + ".write.jdbc.url");
		conf.userName = property.getProperty(dataSource + ".write.jdbc.username");
		conf.password = property.getProperty(dataSource + ".write.jdbc.password");

		conf.initialPoolSize = property.getProperty(dataSource + ".hold.jdbc.initialPoolSize");
		conf.acquireIncrement = property.getProperty(dataSource + ".hold.jdbc.acquireIncrement");
		conf.maxPoolSize = property.getProperty(dataSource + ".hold.jdbc.poolsizemax");
		conf.minPoolSize = property.getProperty(dataSource + ".hold.jdbc.poolsizemin");

		conf.checkoutTimeout = property.getProperty(dataSource + ".hold.jdbc.checkoutTimeout");
		conf.maxStatements = property.getProperty(dataSource + ".hold.jdbc.maxStatements");
		conf.maxStatementsPerConnection = property.getProperty(dataSource + ".hold.jdbc.maxStatementsPerConnection");
		conf.maxIdleTime = property.getProperty(dataSource + ".hold.jdbc.maxIdleTime");

		conf.unreturnedConnectionTimeout = property.getProperty(dataSource + ".hold.jdbc.unreturnedConnectionTimeout");
		String unreturnedConnectionTimeout = conf.unreturnedConnectionTimeout;
		if (Long.valueOf(unreturnedConnectionTimeout) > JDBCConstants.getHoldTimeOut()) {
			JDBCConstants.setHoldTimeOut(Long.valueOf(unreturnedConnectionTimeout));
		}
		return conf;
	}

	public PoolConfig createWriteConf(Properties property) {
		PoolConfig conf = new PoolConfig();
		conf.driverClass = property.getProperty("jdbc.driver");

		conf.jdbcUrl = property.getProperty(dataSource + ".write.jdbc.url");
		conf.userName = property.getProperty(dataSource + ".write.jdbc.username");
		conf.password = property.getProperty(dataSource + ".write.jdbc.password");

		conf.initialPoolSize = property.getProperty(dataSource + ".write.jdbc.initialPoolSize");
		conf.acquireIncrement = property.getProperty(dataSource + ".write.jdbc.acquireIncrement");
		conf.maxPoolSize = property.getProperty(dataSource + ".write.jdbc.poolsizemax");
		conf.minPoolSize = property.getProperty(dataSource + ".write.jdbc.poolsizemin");

		conf.checkoutTimeout = property.getProperty(dataSource + ".write.jdbc.checkoutTimeout");
		conf.maxStatements = property.getProperty(dataSource + ".write.jdbc.maxStatements");
		conf.maxStatementsPerConnection = property.getProperty(dataSource + ".write.jdbc.maxStatementsPerConnection");
		conf.maxIdleTime = property.getProperty(dataSource + ".write.jdbc.maxIdleTime");
		conf.unreturnedConnectionTimeout = property.getProperty(dataSource + ".write.jdbc.unreturnedConnectionTimeout");
		return conf;
	}

	public List<PoolConfig> createReadConfs(Properties property) {
		List<PoolConfig> confs = new ArrayList<PoolConfig>();
		int index = 0;
		while (true) {
			PoolConfig conf = new PoolConfig();
			String pref = dataSource + ".read" + index + ".";
			conf.jdbcUrl = property.getProperty(pref + "jdbc.url");
			conf.userName = property.getProperty(pref + "jdbc.username");
			conf.password = property.getProperty(pref + "jdbc.password");
			String w = property.getProperty(pref + "weight");
			if (StringUtils.isNotBlank(w)) {
				conf.weight = Integer.parseInt(w);
			}

			conf.driverClass = property.getProperty("jdbc.driver");

			conf.initialPoolSize = property.getProperty(dataSource + ".read.jdbc.initialPoolSize");
			conf.acquireIncrement = property.getProperty(dataSource + ".read.jdbc.acquireIncrement");
			conf.maxPoolSize = property.getProperty(dataSource + ".read.jdbc.poolsizemax");
			conf.minPoolSize = property.getProperty(dataSource + ".read.jdbc.poolsizemin");
			conf.checkoutTimeout = property.getProperty(dataSource + ".read.jdbc.checkoutTimeout");
			conf.maxStatements = property.getProperty(dataSource + ".read.jdbc.maxStatements");
			conf.maxStatementsPerConnection = property
					.getProperty(dataSource + ".read.jdbc.maxStatementsPerConnection");
			conf.maxIdleTime = property.getProperty(dataSource + ".read.jdbc.maxIdleTime");
			conf.unreturnedConnectionTimeout = property
					.getProperty(dataSource + ".read.jdbc.unreturnedConnectionTimeout");
			if (!conf.isValid()) {
				break;
			}
			confs.add(conf);
			index++;
		}
		return confs;
	}
}
