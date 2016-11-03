package net.jahhan.db.conn;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.PooledDataSource;

import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;

public class DataSource {
    public final static int MAIN_WRITE = 1;

    public final static int MAIN_READ = 2;

    public final static int BATCH = 3;

    private final int type;

    private boolean readOnly = false;

    private PooledDataSource cpds = new ComboPooledDataSource();

    private final String url;

    private static Logger logger = LoggerFactory.getLogger("DataSource");

    private PooledDataSource createDataPool(PoolConfig conf) {
        if (!conf.isValid()) {
            throw new RuntimeException("数据库驱动为空或者数据库用户名或者密码或者连接字符串为空");
        }
        logger.info("db config:{}", conf.toString());
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

    public DataSource(PoolConfig conf, boolean readOnly, int type) {
        super();
        url = conf.jdbcUrl;
        this.cpds = this.createDataPool(conf);
        this.readOnly = readOnly;
        this.type = type;
    }

    public Connection getConnection() throws SQLException {
        Connection dbConn = this.cpds.getConnection();
        dbConn = new ConnectionWarpper(dbConn, type);
        dbConn.setAutoCommit(false);
        if (this.readOnly) {
            dbConn.setReadOnly(true);
        }
        if (ApplicationContext.CTX.getThreadLocalUtil() != null) {
            InvocationContext ic = ApplicationContext.CTX.getInvocationContext();
            if (ic != null) {
                ic.addDbCon(dbConn);
            }
        }
        DBConnFactory.incGetConn();
        return dbConn;
    }

    public void close() throws SQLException {
        this.cpds.close();
    }

    public String getStatus() throws SQLException {
        StringBuilder sb = new StringBuilder(20);
        sb.append(url);
        sb.append("\nbusyConnection:" + cpds.getNumBusyConnectionsDefaultUser());
        sb.append("\nidleConnection:" + cpds.getNumIdleConnectionsDefaultUser());
        sb.append("\nEffectivePropertyCycle:" + cpds.getEffectivePropertyCycleDefaultUser());
        sb.append("\nLastIdleTestFailure:" + cpds.getLastIdleTestFailureDefaultUser());
        sb.append("\nNumFailedCheckins:" + cpds.getNumFailedCheckinsDefaultUser());
        sb.append("\nNumFailedCheckouts:" + cpds.getNumFailedCheckoutsDefaultUser());
        sb.append("\nNumFailedIdleTests:" + cpds.getNumFailedIdleTestsDefaultUser());
        sb.append("\nNumThreadsAwaitingCheckout:" + cpds.getNumThreadsAwaitingCheckoutDefaultUser());
        sb.append("\nThreadPoolNumActiveThreads:" + cpds.getThreadPoolNumActiveThreads());
        sb.append("\nThreadPoolNumIdleThreads:" + cpds.getThreadPoolNumIdleThreads());
        sb.append("\nThreadPoolNumTasksPending:" + cpds.getThreadPoolNumTasksPending());
        sb.append("\nLastAcquisitionFailure:" + cpds.getLastAcquisitionFailureDefaultUser());
        sb.append("\nLastCheckoutFailure:" + cpds.getLastCheckoutFailureDefaultUser());
        sb.append("\nLastCheckinFailure:" + cpds.getLastCheckinFailureDefaultUser());
        sb.append("\nNumStatements:" + cpds.getStatementCacheNumStatementsDefaultUser());
        sb.append("\nNumConnectionsWithCachedStatements:" + cpds.getStatementCacheNumConnectionsWithCachedStatementsDefaultUser());
        return sb.toString();
    }

    public void reset() {
        try {
            this.cpds.softResetDefaultUser();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
