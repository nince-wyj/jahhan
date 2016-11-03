package net.jahhan.dblogistics.doc;

import com.mongodb.client.MongoDatabase;

import net.jahhan.dblogistics.DBConnExecutorHandler;

/**
 * 数据事务操作
 * 
 * @author nince
 */
public interface DocConnExecutorHandler extends DBConnExecutorHandler {
	MongoDatabase getMongoDatabase();
}
