package net.jahhan.dblogistics.dbconnexecutor;

import java.util.List;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;

import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.event.DBEvent;
import net.jahhan.dblogistics.DBConnExecutorHandler;
import net.jahhan.dblogistics.DblogisticContext;
import net.jahhan.dblogistics.constant.DBLogisticsConf;
import net.jahhan.dblogistics.doc.DocConnExecutorHandler;
import net.jahhan.dblogistics.doc.dbconnexecutor.DocWriteDBConnExecutor;
import net.jahhan.dblogistics.utils.SessionUtils;

public class DBLogisticWriteConnExecutor implements DBConnExecutorHandler {

	@Override
	public void beginConnection() {
		Session neo4jSession = SessionUtils.getNeoWriteSession();
		Transaction transaction = neo4jSession.beginTransaction();
		InvocationContext invocationContext = ApplicationContext.CTX.getInvocationContext();
		invocationContext.setSession("neoTransaction", transaction);
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			DocConnExecutorHandler docConnExecutorHandler = new DocWriteDBConnExecutor(
					docWriteExecutor.getMongoDatabase());
			docConnExecutorHandler.beginConnection();
		}
	}

	@Override
	public void commit() {
		InvocationContext invocationContext = ApplicationContext.CTX.getInvocationContext();
		Transaction transaction = (Transaction) invocationContext.getSession("neoTransaction");
		transaction.commit();
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			DocConnExecutorHandler docConnExecutorHandler = new DocWriteDBConnExecutor(
					docWriteExecutor.getMongoDatabase());
			docConnExecutorHandler.commit();
		}
		List<DBEvent> events = invocationContext.getEvents();
		for (DBEvent event : events) {
			DblogisticContext.instance().realPublishWrite(event);
		}
		ApplicationContext.CTX.getInvocationContext().clearLocalCache();
	}

	@Override
	public void rollback() {
		InvocationContext invocationContext = ApplicationContext.CTX.getInvocationContext();
		Transaction transaction = (Transaction) invocationContext.getSession("neoTransaction");
		transaction.rollback();
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			DocConnExecutorHandler docConnExecutorHandler = new DocWriteDBConnExecutor(
					docWriteExecutor.getMongoDatabase());
			docConnExecutorHandler.rollback();
		}
		List<DBEvent> events = invocationContext.getEvents();
		events.clear();
		ApplicationContext.CTX.getInvocationContext().clearLocalCache();
	}

	@Override
	public void close() {
		InvocationContext invocationContext = ApplicationContext.CTX.getInvocationContext();
		Transaction transaction = (Transaction) invocationContext.getSession("neoTransaction");
		transaction.close();
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			DocConnExecutorHandler docConnExecutorHandler = new DocWriteDBConnExecutor(
					docWriteExecutor.getMongoDatabase());
			docConnExecutorHandler.close();
		}
	}
}
