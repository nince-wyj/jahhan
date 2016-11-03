package net.jahhan.init.module;

import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import net.jahhan.init.InitAnnocation;
import net.jahhan.job.api.TaskProcessor;
import net.jahhan.job.api.impl.TaskProcessorImpl;

@InitAnnocation(isLazy = false, initSequence = 5100)
public class QuarzModule extends AbstractModule {
	@Override
	protected void configure() {
		this.bind(SchedulerFactory.class).to(StdSchedulerFactory.class).in(Scopes.SINGLETON);
		this.bind(TaskProcessor.class).to(TaskProcessorImpl.class).in(Scopes.SINGLETON);
	}
}
