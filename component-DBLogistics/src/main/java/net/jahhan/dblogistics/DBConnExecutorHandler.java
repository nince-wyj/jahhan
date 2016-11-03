package net.jahhan.dblogistics;

/**
 * 数据事务操作
 * 
 * @author nince
 */
public interface DBConnExecutorHandler {
    void beginConnection();

    void commit();

    void rollback();

    void close();
}
