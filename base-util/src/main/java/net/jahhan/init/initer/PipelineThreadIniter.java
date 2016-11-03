package net.jahhan.init.initer;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.enumeration.ThreadPoolEnum;
import net.jahhan.factory.ThreadPoolFactory;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.thread.PipelineHttpThread;

@InitAnnocation(isLazy = false, initSequence = 2100)
public class PipelineThreadIniter implements BootstrapInit {
	@Inject
	private ThreadPoolFactory threadPoolFactory;

	@Override
	public void execute() {
		List<String> pipelineOriginList = SysConfiguration.getPipelineOriginList();
		ScheduledExecutorService executeService = (ScheduledExecutorService) threadPoolFactory
				.getExecuteService(ThreadPoolEnum.SCHEDULED);
		for (String host : pipelineOriginList) {
			PipelineHttpThread pipelineHttpThread = new PipelineHttpThread(host);
			executeService.scheduleWithFixedDelay(pipelineHttpThread, 3, 2, TimeUnit.SECONDS);
		}
	}
}
