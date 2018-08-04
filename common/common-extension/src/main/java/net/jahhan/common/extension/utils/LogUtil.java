package net.jahhan.common.extension.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
	private static final Logger exceptionLog = LoggerFactory.getLogger("message.record.exception");
	private static final Logger requestExceptionLog = LoggerFactory.getLogger("message.request.exception");
	private static final Logger requestLog = LoggerFactory.getLogger("message.request.info");
	private static final Logger lockLog = LoggerFactory.getLogger("message.lock.info");

	public static void error(String msg) {
		exceptionLog.error(msg);
	}

	public static void error(String msg, Throwable t) {
		exceptionLog.error(msg, t);
	}

	public static void requestInfo(String msg) {
		requestLog.info(msg);
	}

	public static void requestError(String msg) {
		requestExceptionLog.error(msg);
	}

	public static void requestError(String msg, Throwable t) {
		requestExceptionLog.error(msg, t);
	}

	public static void lockInfo(String msg) {
		lockLog.info(msg);
	}

	public static void lockInfo(String msg, Throwable t) {
		lockLog.info(msg, t);
	}
}
