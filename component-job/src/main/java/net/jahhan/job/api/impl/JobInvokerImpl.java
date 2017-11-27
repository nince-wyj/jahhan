package net.jahhan.job.api.impl;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.jahhan.annotation.Job;
import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.conn.DBConnFactory;
import net.jahhan.db.dbconnexecutor.DBConnExecutorFactory;
import net.jahhan.job.api.JobInvoker;
import net.jahhan.job.api.JobTask;

public class JobInvokerImpl implements JobInvoker {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobDetail = (JobDetail) context.getJobDetail();
		Class<? extends JobTask> jobClass = jobDetail.getJobTaskClass();
		Job jobAnnotation = jobClass.getAnnotation(Job.class);
		JobTask jobTask = jobDetail.getInjector().getInstance(jobClass);

		BaseContext applicationContext = BaseContext.CTX;
		InvocationContext invocationContext = new InvocationContext();
		applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);
		DBConnectionType dBConnectionType = jobAnnotation.value();
		invocationContext.setConnectionType(dBConnectionType);
		DBConnExecutorFactory connExec = new DBConnExecutorFactory(dBConnectionType);
		try {
			connExec.beginConnection();
			jobTask.excute();
			connExec.endConnection();
		} catch (Exception e) {
			connExec.rollback();
		} catch (Error e) {
			connExec.rollback();
		} finally {
			connExec.close();
			DBConnFactory.freeConns(invocationContext.getConnections());
		}

	}
}