package net.jahhan.test;

import java.sql.Connection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.google.inject.Injector;

import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.conn.DBConnFactory;
import net.jahhan.db.dbconnexecutor.DBConnExecutorFactory;
import net.jahhan.init.InitMethod;

public abstract class AbstractTestMethod {
	DBConnectionType dbType = DBConnectionType.READ;
	protected static Injector injector;
	DBConnExecutorFactory connExec;

	public void setDBConnectionType(DBConnectionType dbType) {
		this.dbType = dbType;
	}

	public Injector getInjector() {
		return injector;
	}

	@BeforeClass
	public static void init() throws Exception {
		new InitMethod(false) {
			@Override
			public void init() {
				injector = getInjector();
				super.init();
			}
		}.init();
	}

	@Before
	public void setUp() throws Exception {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = new InvocationContext(null, null);
		applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);
		connExec = new DBConnExecutorFactory(dbType);
		connExec.beginConnection();
	}

	@After
	public void tearDown() throws Exception {
		connExec.endConnection();
	}

	@AfterClass
	public static void finish() throws Exception {
		Connection[] conns = ApplicationContext.CTX.getInvocationContext().getConnections()
				.toArray(new Connection[0]);
		if (conns != null) {
			for (Connection conn : conns) {
				DBConnFactory.freeConnection(conn);
			}
		}
	}
}
