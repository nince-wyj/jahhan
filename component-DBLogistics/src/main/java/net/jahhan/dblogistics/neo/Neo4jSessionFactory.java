package net.jahhan.dblogistics.neo;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.DBLogisticsConnectionType;
import net.jahhan.exception.FrameworkException;
import net.jahhan.utils.PropertiesUtil;

public class Neo4jSessionFactory {

	private final static SessionFactory writeFactory;
	private final static SessionFactory readFactory;

	static {
		Configuration writeConf = new Configuration();
		writeConf.driverConfiguration().setDriverClassName(PropertiesUtil.get("dblogistic", "neo.driverClassName"))
				.setURI(PropertiesUtil.get("dblogistic", "neo.writeUri")).setConnectionPoolSize(200);
		Configuration readConf = new Configuration();
		readConf.driverConfiguration().setDriverClassName(PropertiesUtil.get("dblogistic", "neo.driverClassName"))
				.setURI(PropertiesUtil.get("dblogistic", "neo.readUri")).setConnectionPoolSize(500);
		String packages = PropertiesUtil.get("dblogistic", "entity.packages");
		packages = packages + ",net.jahhan.dblogistics.entity";
		writeFactory = new SessionFactory(writeConf, packages.split(","));
		readFactory = new SessionFactory(readConf, packages.split(","));
	}

	private static Neo4jSessionFactory factory = new Neo4jSessionFactory();

	public static Neo4jSessionFactory getInstance() {
		return factory;
	}

	private Neo4jSessionFactory() {
	}

	public Session getNeo4jSession(DBLogisticsConnectionType connType) {
		switch (connType) {
		case READ:
			return readFactory.openSession();
		case WRITE:
			return writeFactory.openSession();
		default:
			FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "错误的连接类型！！");
		}
		return null;
	}
}