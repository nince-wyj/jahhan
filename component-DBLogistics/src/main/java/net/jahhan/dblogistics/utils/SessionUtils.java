package net.jahhan.dblogistics.utils;

import org.neo4j.ogm.session.Session;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.DBLogisticsConnectionType;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.dblogistics.doc.DocClientFactory;
import net.jahhan.dblogistics.doc.DocConnExecutorHandler;
import net.jahhan.dblogistics.doc.dbconnexecutor.DocWriteDBConnExecutor;
import net.jahhan.dblogistics.neo.Neo4jSessionFactory;
import net.jahhan.exception.FrameworkException;
import net.jahhan.utils.PropertiesUtil;

public class SessionUtils {
	public static Session getNeoWriteSession() {
		InvocationContext invocationContext = ApplicationContext.CTX.getInvocationContext();
		Session session = (Session) invocationContext.getSession("neoWrite");
		if (null == session) {
			session = Neo4jSessionFactory.getInstance().getNeo4jSession(DBLogisticsConnectionType.WRITE);
			invocationContext.setSession("neoWrite", session);
		}
		return session;
	}

	public static Session getNeoReadSession() {
		InvocationContext invocationContext = ApplicationContext.CTX.getInvocationContext();
		DBLogisticsConnectionType dbLogisticsConnType = invocationContext.getDBLogisticsConnType();
		switch (dbLogisticsConnType) {
		case NONE: {
			FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "数据库类型错误！");
			break;
		}
		case READ: {
			Session session = (Session) invocationContext.getSession("neoRead");
			if (null == session) {
				session = Neo4jSessionFactory.getInstance().getNeo4jSession(DBLogisticsConnectionType.READ);
				invocationContext.setSession("neoRead", session);
			}
			return session;
		}
		case WRITE: {
			Session session = (Session) invocationContext.getSession("neoWrite");
			if (null == session) {
				session = Neo4jSessionFactory.getInstance().getNeo4jSession(DBLogisticsConnectionType.WRITE);
				invocationContext.setSession("neoWrite", session);
			}
			return session;
		}
		default:
			break;
		}
		return null;
	}

	public static DocConnExecutorHandler getDocReadExecutor() {
		InvocationContext invocationContext = ApplicationContext.CTX.getInvocationContext();
		DBLogisticsConnectionType dbLogisticsConnType = invocationContext.getDBLogisticsConnType();
		switch (dbLogisticsConnType) {
		case NONE: {
			FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "数据库类型错误！");
			break;
		}
		case READ: {
			DocConnExecutorHandler dbConnExecutorHandler = (DocConnExecutorHandler) invocationContext
					.getSession("docRead");
			if (null == dbConnExecutorHandler) {
				MongoClient client = DocClientFactory.getInstance().getClient();
				MongoDatabase db = client.getDatabase(PropertiesUtil.get("dblogistic", "doc.dataBase"));
				dbConnExecutorHandler = new DocWriteDBConnExecutor(db);
				invocationContext.setSession("docRead", dbConnExecutorHandler);
			}
			return dbConnExecutorHandler;
		}
		case WRITE: {
			DocConnExecutorHandler dbConnExecutorHandler = (DocConnExecutorHandler) invocationContext
					.getSession("docWrite");
			if (null == dbConnExecutorHandler) {
				MongoClient client = DocClientFactory.getInstance().getClient();
				MongoDatabase db = client.getDatabase(PropertiesUtil.get("dblogistic", "doc.dataBase"));
				dbConnExecutorHandler = new DocWriteDBConnExecutor(db);
				invocationContext.setSession("docWrite", dbConnExecutorHandler);
			}
			return dbConnExecutorHandler;
		}
		default:
			break;
		}
		return null;
	}

	public static DocConnExecutorHandler getDocWriteExecutor() {
		InvocationContext invocationContext = ApplicationContext.CTX.getInvocationContext();
		DocConnExecutorHandler dbConnExecutorHandler = (DocConnExecutorHandler) invocationContext
				.getSession("docWrite");
		if (null == dbConnExecutorHandler) {
			MongoClient client = DocClientFactory.getInstance().getClient();
			MongoDatabase db = client.getDatabase(PropertiesUtil.get("dblogistic", "doc.dataBase"));
			
			dbConnExecutorHandler = new DocWriteDBConnExecutor(db);
			invocationContext.setSession("docWrite", dbConnExecutorHandler);
		}
		return dbConnExecutorHandler;
	}
}
