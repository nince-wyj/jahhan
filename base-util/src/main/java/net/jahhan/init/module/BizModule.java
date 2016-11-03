package net.jahhan.init.module;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import net.jahhan.annotation.Biz;
import net.jahhan.annotation.DbConn;
import net.jahhan.annotation.Transaction;
import net.jahhan.init.DBConnInterceptor;
import net.jahhan.init.InitAnnocation;
import net.jahhan.init.TransactionInterceptor;

@InitAnnocation(isLazy = false, initSequence = 3000)
public class BizModule extends AbstractModule {

	@Override
	public void configure() {
		bindInterceptor(Matchers.annotatedWith(Biz.class), Matchers.annotatedWith(DbConn.class),
				new DBConnInterceptor());
		bindInterceptor(Matchers.annotatedWith(Biz.class), Matchers.annotatedWith(Transaction.class),
				new TransactionInterceptor());
	}

}
