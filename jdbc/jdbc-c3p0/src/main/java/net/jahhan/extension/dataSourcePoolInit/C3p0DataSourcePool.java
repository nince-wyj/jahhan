package net.jahhan.extension.dataSourcePoolInit;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Singleton;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.jdbc.conn.C3p0DataSourceWrapper;
import net.jahhan.jdbc.conn.DataSourceConnectionPool;
import net.jahhan.jdbc.conn.DataSourcePool;
import net.jahhan.jdbc.conn.DataSourceWrapper;
import net.jahhan.jdbc.conn.PoolConfig;
import net.jahhan.jdbc.constant.enumeration.DBConnectLevel;

@Singleton
@Extension("c3p0")
public class C3p0DataSourcePool extends DataSourcePool {
	public void initDataSource(String dataSource, Properties property) throws PropertyVetoException {
		DataSourceConnectionPool dataSourceConnectionPool = dataSourceConnectionMap.get(dataSource);

		PoolConfig batch = dataSourceConnectionPool.createBatchConf(property);
		if (batch.jdbcUrl.contains("?")) {
			batch.jdbcUrl = batch.jdbcUrl + "&rewriteBatchedStatements=true";
		} else {
			batch.jdbcUrl = batch.jdbcUrl + "?rewriteBatchedStatements=true";
		}
		dataSourceConnectionPool.setBatchDS(new C3p0DataSourceWrapper(batch, false, DBConnectLevel.BATCH.getLevel()));

		PoolConfig write = dataSourceConnectionPool.createWriteConf(property);
		dataSourceConnectionPool.setWriteDS(new C3p0DataSourceWrapper(write, false, DBConnectLevel.WRITE.getLevel()));

		PoolConfig hold = dataSourceConnectionPool.createHoldConf(property);
		dataSourceConnectionPool.setHoldDS(new C3p0DataSourceWrapper(hold, false, DBConnectLevel.WRITE.getLevel()));
		
		List<PoolConfig> readConfs = dataSourceConnectionPool.createReadConfs(property);
		List<DataSourceWrapper> readDSList = new ArrayList<>();
		for (PoolConfig conf : readConfs) {
			C3p0DataSourceWrapper ds = new C3p0DataSourceWrapper(conf, true, DBConnectLevel.READ.getLevel());
			for (int i = 0; i < conf.weight; i++) {
				readDSList.add(ds);
			}
		}
		dataSourceConnectionPool.setReadDSList(readDSList);
	}
}
