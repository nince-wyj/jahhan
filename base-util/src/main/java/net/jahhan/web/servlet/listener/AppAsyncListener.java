package net.jahhan.web.servlet.listener;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@WebListener
public class AppAsyncListener implements AsyncListener {
	private final static Logger logger = LoggerFactory.getLogger("AsyncDecodeDataHandler");

	@Override
	public void onComplete(AsyncEvent asyncEvent) throws IOException {
		logger.debug("AppAsyncListener onComplete");
	}

	@Override
	public void onError(AsyncEvent asyncEvent) throws IOException {
		logger.debug("AppAsyncListener onError");
	}

	@Override
	public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
		logger.debug("AppAsyncListener onStartAsync");
	}

	@Override
	public void onTimeout(AsyncEvent asyncEvent) throws IOException {
		logger.debug("AppAsyncListener onTimeout");
//		ServletResponse response = asyncEvent.getAsyncContext().getResponse();
//		PrintWriter out = response.getWriter();
//		out.write("TimeOut Error in Processing");
	}

}