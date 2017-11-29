package net.jahhan.init.module.jdbc;

import com.google.inject.AbstractModule;

import net.jahhan.init.InitAnnocation;
import net.jahhan.jdbc.PublisherHandler;
import net.jahhan.jdbc.SessionHandler;
import net.jahhan.jdbc.context.DBContext;
import net.jahhan.jdbc.mybaitssession.DBSessionHelper;
import net.jahhan.jdbc.publish.DBPublisherHandler;

@InitAnnocation(isLazy = false, initSequence = 4600)
public class AppContextBindModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(SessionHandler.class).to(DBSessionHelper.class);
		bind(PublisherHandler.class).to(DBPublisherHandler.class);
		bind(DBContext.class);
	}
}
