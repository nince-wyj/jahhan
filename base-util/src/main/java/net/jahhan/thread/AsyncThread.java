package net.jahhan.thread;

import java.util.concurrent.ExecutorService;

import javax.servlet.AsyncContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

import net.jahhan.constant.enumeration.ThreadPoolEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.factory.ThreadPoolFactory;
import net.jahhan.handler.WorkHandler;

/**
 * 接口异步线程方法
 * 
 * @author nince
 */
public class AsyncThread implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger("AsyncThread");

	@Inject
	private Injector injector;
	@Inject
	private ThreadPoolFactory threadPoolFactory;

	private InvocationContext invocationContext;
	private WorkHandler service;
	private AsyncContext asyncCtx;
	private String actName;

	public void setActName(String actName) {
		this.actName = actName;
	}

	public void setInvocationContext(InvocationContext invocationContext) {
		this.invocationContext = invocationContext;
	}

	public void setService(WorkHandler service) {
		this.service = service;
	}

	public void setAsyncCtx(AsyncContext asyncCtx) {
		this.asyncCtx = asyncCtx;
	}

	public void setConsumeTime(long startTime, String actName) {
		long now = System.currentTimeMillis();
		long ms = now - startTime;
		ResponseConsumeThread t = injector.getInstance(ResponseConsumeThread.class);
		t.setActName(actName);
		t.setConsumeTime(ms);
		ExecutorService executeService = threadPoolFactory.getExecuteService(ThreadPoolEnum.FIXED);
		executeService.execute(t);
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		ApplicationContext applicationContext = ApplicationContext.CTX;
		try {
			invocationContext.setAsyncClient(true);
			invocationContext.setAsyncCtx(asyncCtx);
			applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);
			service.execute();
			setConsumeTime(startTime, actName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} catch (Error e) {
			logger.error(e.getMessage(), e);
		}
		synchronized (invocationContext.getAsyncHttpCallBackList()) {
			if (invocationContext.asyncHttpCallBackListSize() == 0) {
				asyncCtx.complete();
			}
		}
	}
}
