package net.jahhan.db.dbconnexecutor;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.db.DBConnExecutorHandler;

public class NoneDBConnExecutor implements DBConnExecutorHandler {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Connection beginConnection() {
        return null;
    }

    @Override
    public void commit() throws SQLException {

    }

    @Override
    public void rollback() {
    }

    @Override
    public void close() {

    }

}
