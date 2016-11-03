package net.jahhan.dblogistics.dbconnexecutor;

import net.jahhan.dblogistics.DBConnExecutorHandler;
import net.jahhan.dblogistics.constant.DBLogisticsConf;
import net.jahhan.dblogistics.doc.DocConnExecutorHandler;
import net.jahhan.dblogistics.doc.dbconnexecutor.DocReadDBConnExecutor;
import net.jahhan.dblogistics.utils.SessionUtils;

public class DBLogisticReadConnExecutor implements DBConnExecutorHandler {

	@Override
	public void beginConnection() {
		SessionUtils.getNeoReadSession();
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			DocConnExecutorHandler docConnExecutorHandler = new DocReadDBConnExecutor(
					docReadExecutor.getMongoDatabase());
			docConnExecutorHandler.beginConnection();
		}
	}

	@Override
	public void commit() {
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			DocConnExecutorHandler docConnExecutorHandler = new DocReadDBConnExecutor(
					docReadExecutor.getMongoDatabase());
			docConnExecutorHandler.commit();
		}
	}

	@Override
	public void rollback() {
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			DocConnExecutorHandler docConnExecutorHandler = new DocReadDBConnExecutor(
					docReadExecutor.getMongoDatabase());
			docConnExecutorHandler.rollback();
		}
	}

	@Override
	public void close() {
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			DocConnExecutorHandler docConnExecutorHandler = new DocReadDBConnExecutor(
					docReadExecutor.getMongoDatabase());
			docConnExecutorHandler.close();
		}
	}
}
