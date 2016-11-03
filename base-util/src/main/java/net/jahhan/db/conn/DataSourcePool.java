package net.jahhan.db.conn;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.utils.PropertiesUtil;

public class DataSourcePool {
	private static Logger logger = LoggerFactory.getLogger("DataSourcePool");

	private DataSource mainDS;

	private DataSource batchDS;

	private final List<DataSource> readDSList = new ArrayList<DataSource>();

	private static volatile int readCount = new Random().nextInt(100);

	private AtomicInteger ai = new AtomicInteger();

	public Connection getMainConnection() throws SQLException {
		return mainDS.getConnection();
	}

	public Connection getBatchConnection() throws SQLException {
		return batchDS.getConnection();
	}

	public String getStatus() throws SQLException {
		Set<DataSource> set = new HashSet<DataSource>();
		set.addAll(readDSList);
		set.add(mainDS);
		set.add(batchDS);
		StringBuilder sb = new StringBuilder();
		for (DataSource ds : set) {
			sb.append(ds.getStatus() + "\n");
		}
		return sb.toString();
	}

	public synchronized int getReadConnIdx() {
		int i = ai.getAndIncrement();
		ai.compareAndSet(readDSList.size(), 0);
		return i;
	}

	public Connection getReadConnection() throws SQLException {
		int count = readCount++;
		if (count < 0) {
			readCount = 0;
			count = count == Integer.MIN_VALUE ? 0 : 0 - count;
		}

		int index = getReadConnIdx();
		// int index = readCount % readDSList.size();
		logger.debug("读库index={}", index);
		return readDSList.get(index).getConnection();
	}

	// private PoolConfig createHisConf(Properties property) {
	// PoolConfig conf = new PoolConfig();
	// conf.driverClass = property.getProperty("his.driver");
	// conf.jdbcUrl = property.getProperty("his.url");
	// conf.userName = property.getProperty("his.username");
	// conf.password = property.getProperty("his.password");
	// conf.initialPoolSize = property.getProperty("his.initialPoolSize");
	// conf.acquireIncrement = property.getProperty("his.acquireIncrement");
	// conf.maxPoolSize = property.getProperty("his.poolsizemax");
	// conf.minPoolSize = property.getProperty("his.poolsizemin");
	//
	// conf.checkoutTimeout = property.getProperty("his.checkoutTimeout");
	// conf.maxStatements = property.getProperty("his.maxStatements");
	// conf.maxStatementsPerConnection =
	// property.getProperty("his.maxStatementsPerConnection");
	// conf.maxIdleTime = property.getProperty("his.maxIdleTime");
	// conf.unreturnedConnectionTimeout =
	// property.getProperty("his.unreturnedConnectionTimeout");
	// if (!conf.isValid()) {
	// return null;
	// }
	// return conf;
	// }

	private PoolConfig createDefaultConf(Properties property) {
		PoolConfig conf = new PoolConfig();
		conf.driverClass = property.getProperty("jdbc.driver");
		conf.jdbcUrl = property.getProperty("jdbc.url");
		conf.userName = property.getProperty("jdbc.username");
		conf.password = property.getProperty("jdbc.password");
		conf.initialPoolSize = property.getProperty("jdbc.initialPoolSize");
		conf.acquireIncrement = property.getProperty("jdbc.acquireIncrement");
		conf.maxPoolSize = property.getProperty("jdbc.poolsizemax");
		conf.minPoolSize = property.getProperty("jdbc.poolsizemin");

		conf.checkoutTimeout = property.getProperty("jdbc.checkoutTimeout");
		conf.maxStatements = property.getProperty("jdbc.maxStatements");
		conf.maxStatementsPerConnection = property.getProperty("jdbc.maxStatementsPerConnection");
		conf.maxIdleTime = property.getProperty("jdbc.maxIdleTime");
		conf.unreturnedConnectionTimeout = property.getProperty("jdbc.unreturnedConnectionTimeout");
		return conf;
	}

	private List<PoolConfig> createReadConfs(Properties property) {
		List<PoolConfig> confs = new ArrayList<PoolConfig>();
		int index = 0;
		while (true) {
			PoolConfig conf = new PoolConfig();
			String pref = "read" + index + ".";
			conf.jdbcUrl = property.getProperty(pref + "jdbc.url");
			conf.userName = property.getProperty(pref + "jdbc.username");
			conf.password = property.getProperty(pref + "jdbc.password");
			String w = property.getProperty(pref + "weight");
			if (StringUtils.isNotBlank(w)) {
				conf.weight = Integer.parseInt(w);
			}

			conf.driverClass = property.getProperty("jdbc.driver");
			conf.initialPoolSize = property.getProperty("read.jdbc.initialPoolSize");
			conf.acquireIncrement = property.getProperty("read.jdbc.acquireIncrement");
			conf.maxPoolSize = property.getProperty("read.jdbc.poolsizemax");
			conf.minPoolSize = property.getProperty("read.jdbc.poolsizemin");
			conf.checkoutTimeout = property.getProperty("read.jdbc.checkoutTimeout");
			conf.maxStatements = property.getProperty("read.jdbc.maxStatements");
			conf.maxStatementsPerConnection = property.getProperty("read.jdbc.maxStatementsPerConnection");
			conf.maxIdleTime = property.getProperty("read.jdbc.maxIdleTime");
			conf.unreturnedConnectionTimeout = property.getProperty("read.jdbc.unreturnedConnectionTimeout");
			if (!conf.isValid()) {
				break;
			}
			confs.add(conf);
			index++;
		}
		return confs;
	}

	private void createPoolConfigs(Properties property) throws PropertyVetoException {
		PoolConfig main = this.createDefaultConf(property);
		mainDS = new DataSource(main, false, DataSource.MAIN_WRITE);
		PoolConfig batch = this.createDefaultConf(property);
		if (main.jdbcUrl.contains("?")) {
			batch.jdbcUrl = main.jdbcUrl + "&rewriteBatchedStatements=true";
		} else {
			batch.jdbcUrl = main.jdbcUrl + "?rewriteBatchedStatements=true";
		}
		batchDS = new DataSource(batch, false, DataSource.BATCH);
		List<PoolConfig> readConfs = this.createReadConfs(property);
		for (PoolConfig conf : readConfs) {
			DataSource ds = new DataSource(conf, true, DataSource.MAIN_READ);
			for (int i = 0; i < conf.weight; i++) {
				readDSList.add(ds);
			}
		}
	}

	public static void freeConnection(Connection dbConn) throws SQLException {
		if (dbConn != null && !dbConn.isClosed()) {
			dbConn.close();
		}
	}

	public void init() {
		logger.debug(
				"DataSourcePool init===========================================" + this.getClass().getClassLoader());
		try {
			Properties property = PropertiesUtil
					.getProperties(SysConfiguration.getJdbcFileName());
			createPoolConfigs(property);
		} catch (Exception ex) {
			logger.error("加载系统jdbc.properties出错", ex);
			throw new RuntimeException("加载系统配置出错");
		}
	}
}
