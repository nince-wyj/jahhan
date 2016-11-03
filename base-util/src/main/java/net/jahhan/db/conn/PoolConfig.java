package net.jahhan.db.conn;

public class PoolConfig {

    int weight = 1;

    String maxIdleTime;

    String unreturnedConnectionTimeout;

    String maxStatementsPerConnection;

    String maxStatements;

    String checkoutTimeout;

    String driverClass;

    String jdbcUrl;

    String userName;

    String password;

    String initialPoolSize;

    @Override
    public String toString() {
        return "PoolConfig [driverClass=" + driverClass + ", jdbcUrl=" + jdbcUrl + ", userName=" + userName + ", password=" + password
                + ", minPoolSize=" + minPoolSize + ", maxPoolSize=" + maxPoolSize + ", weight=" + weight + ", maxIdleTime=" + maxIdleTime
                + ", unreturnedConnectionTimeout=" + unreturnedConnectionTimeout + ", maxStatementsPerConnection=" + maxStatementsPerConnection
                + ", maxStatements=" + maxStatements + ", checkoutTimeout=" + checkoutTimeout + ", initialPoolSize=" + initialPoolSize
                + ", acquireIncrement=" + acquireIncrement + "]";
    }

    String acquireIncrement;

    String maxPoolSize;

    String minPoolSize;

    boolean isValid() {
        return driverClass != null && !driverClass.isEmpty() && jdbcUrl != null && !jdbcUrl.isEmpty() && userName != null && !userName.isEmpty()
                && password != null && !password.isEmpty();
    }

}
