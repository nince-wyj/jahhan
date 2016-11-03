package net.jahhan.dblogistics.doc.dbconnexecutor;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;

import net.jahhan.dblogistics.doc.DocConnExecutorHandler;

public class DocReadDBConnExecutor implements DocConnExecutorHandler {
	MongoDatabase db;

	public DocReadDBConnExecutor(MongoDatabase db) {
		this.db = db;
	}

	@Override
	public void beginConnection() {
		BasicDBObject transaction = new BasicDBObject();
		transaction.append("beginTransaction", 1);
		transaction.append("isolation", "mvcc");
		db.runCommand(transaction);
	}

	@Override
	public void commit() {
		BasicDBObject commitTransaction = new BasicDBObject();
		commitTransaction.append("commitTransaction", 1);
		db.runCommand(commitTransaction);
	}

	@Override
	public void rollback() {
		BasicDBObject commitTransaction = new BasicDBObject();
		commitTransaction.append("rollbackTransaction", 1);
		db.runCommand(commitTransaction);
	}

	@Override
	public void close() {

	}

	@Override
	public MongoDatabase getMongoDatabase() {
		return db;
	}
}
