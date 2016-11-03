package net.jahhan.job.api.impl;

import org.quartz.Job;
import org.quartz.impl.JobDetailImpl;

import com.google.inject.Injector;

import net.jahhan.job.api.JobTask;

public class JobDetail extends JobDetailImpl {
	private static final long serialVersionUID = -8514865085050079759L;
	private Injector injector;
	private Class<? extends JobTask> jobTaskClass;

	public JobDetail() {
		super();
	}

	public JobDetail(String name, String group, Class<? extends Job> jobClass, boolean durability, boolean recover) {
		super();
		setName(name);
		setGroup(group);
		setJobClass(jobClass);
		setDurability(durability);
		setRequestsRecovery(recover);
	}

	public JobDetail(String name, String group, Class<? extends Job> jobClass) {
		super();
		setName(name);
		setGroup(group);
		setJobClass(jobClass);
	}

	public void setInjector(final Injector injector) {
		this.injector = injector;
	}

	public Injector getInjector() {
		return this.injector;
	}

	public Class<? extends JobTask> getJobTaskClass() {
		return jobTaskClass;
	}

	public void setJobTaskClass(Class<? extends JobTask> jobTaskClass) {
		this.jobTaskClass = jobTaskClass;
	}

}
