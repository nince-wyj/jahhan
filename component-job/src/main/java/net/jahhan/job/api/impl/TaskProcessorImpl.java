package net.jahhan.job.api.impl;

import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.quartz.CronScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.jahhan.annotation.Job;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.exception.FrameworkException;
import net.jahhan.job.api.JobTask;
import net.jahhan.job.api.TaskProcessor;
import net.jahhan.utils.PropertiesUtil;

public class TaskProcessorImpl implements TaskProcessor {
	private static Logger logger = LoggerFactory.getLogger(TaskProcessorImpl.class);
	@Inject
	private Injector injector;
	@Inject
	private SchedulerFactory factory;

	private Scheduler scheduler;
	private Properties property = PropertiesUtil.getProperties("job");

	@Override
	public void start() throws Exception {
		this.scheduler = factory.getScheduler();
		Set<Object> keySet = property.keySet();
		for (Object key : keySet) {
			try {
				String[] keySplit = key.toString().split("\\.");
				String className = keySplit[0];
				String triggerType = keySplit[1];
				JobDetail jobDetail = new JobDetail(className, Scheduler.DEFAULT_GROUP, JobInvokerImpl.class);
				jobDetail.setInjector(injector);
				@SuppressWarnings("unchecked")
				Class<? extends JobTask> jobTaskClass = (Class<? extends JobTask>) Class
						.forName(SysConfiguration.getCompanyName() + ".job." + className);
				if (!jobTaskClass.isAnnotationPresent(Job.class) || !JobTask.class.isAssignableFrom(jobTaskClass)) {
					FrameworkException.throwException(SystemErrorCode.CODE_ERROR, className + "启动失败！");
				}
				jobDetail.setJobTaskClass(jobTaskClass);
				if (triggerType.equals("repeat")) {
					Trigger trigger = TriggerBuilder.newTrigger().withIdentity(className, Scheduler.DEFAULT_GROUP)
							.startNow()
							.withSchedule(SimpleScheduleBuilder.simpleSchedule()
									.withIntervalInSeconds(Integer.valueOf(property.getProperty(key.toString())))
									.repeatForever())
							.build();
					this.scheduler.scheduleJob(jobDetail, trigger);
				} else if (triggerType.equals("cron")) {
					Trigger trigger = TriggerBuilder.newTrigger().withIdentity(className, Scheduler.DEFAULT_GROUP)
							.startNow()
							.withSchedule(CronScheduleBuilder.cronSchedule(property.getProperty(key.toString())))
							.build();
					this.scheduler.scheduleJob(jobDetail, trigger);
				}else if (triggerType.equals("once")) {
					Trigger trigger = TriggerBuilder.newTrigger().withIdentity(className, Scheduler.DEFAULT_GROUP)
							.startNow()
							.withSchedule(SimpleScheduleBuilder.simpleSchedule()
									.withIntervalInSeconds(Integer.valueOf(property.getProperty(key.toString())))
									.withRepeatCount(0))
							.build();
					this.scheduler.scheduleJob(jobDetail, trigger);
				}
			} catch (FrameworkException e) {
				logger.error(key + "启动失败！", e);
			}
		}

		this.scheduler.start();
	}

	@Override
	public void shutdown() throws Exception {
		if (this.scheduler != null) {
			this.scheduler.shutdown();
		}
	}
}
