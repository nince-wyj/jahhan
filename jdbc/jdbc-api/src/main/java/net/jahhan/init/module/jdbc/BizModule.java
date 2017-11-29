package net.jahhan.init.module.jdbc;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import net.jahhan.common.extension.annotation.Service;
import net.jahhan.init.DBConnInterceptor;
import net.jahhan.init.InitAnnocation;
import net.jahhan.init.TransactionInterceptor;
import net.jahhan.jdbc.annotation.DBConnections;
import net.jahhan.jdbc.annotation.Transaction;

@InitAnnocation(isLazy = false, initSequence = 3000)
public class BizModule extends AbstractModule {

	@Override
	public void configure() {
		bindInterceptor(Matchers.annotatedWith(Service.class), Matchers.annotatedWith(DBConnections.class),
				new DBConnInterceptor());
		bindInterceptor(Matchers.annotatedWith(Service.class), Matchers.annotatedWith(Transaction.class),
				new TransactionInterceptor());
	}

}
