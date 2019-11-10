package net.jahhan.common.extension.exception;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.LocalIpUtils;
import net.jahhan.variable.BaseThreadVariable;

@Data
@EqualsAndHashCode(callSuper = false)
public class HttpException extends JahhanException {

	private static final long serialVersionUID = -3297153548445915405L;
	private static int defaultHttpStatus = 500;

	public int getHttpStatus() {
		return ((HttpExceptionMessage) this.exceptionMessage).getHttpStatus();
	}

	public HttpException(int httpStatus, String code, String msg, ExceptionMessage cause) {
		super(msg);
		this.exceptionMessage = new HttpExceptionMessage();
		((HttpExceptionMessage) this.exceptionMessage).setHttpStatus(httpStatus);
		this.exceptionMessage.setCode(code);
		this.exceptionMessage.setMessage(msg);
		this.exceptionMessage.setService(BaseConfiguration.SERVICE);
		BaseThreadVariable baseVariable = (BaseThreadVariable) BaseThreadVariable.getThreadVariable("base");
		if (null != baseVariable) {
			this.exceptionMessage.setRequestId(baseVariable.getRequestId());
			this.exceptionMessage.setChainId(baseVariable.getChainId());
		}
		this.exceptionMessage.setHost(LocalIpUtils.getFirstIp());
		this.exceptionMessage.setThreadId(Thread.currentThread().getId());
		this.exceptionMessage.setThreadName(Thread.currentThread().getName());
		this.exceptionMessage.setTime(new Date());
		this.exceptionMessage.setCause(cause);
	}

	public HttpException(String code, String msg, ExceptionMessage cause) {
		this(defaultHttpStatus, code, msg, cause);
		((HttpExceptionMessage) this.exceptionMessage).setHttpStatus(defaultHttpStatus);
	}

	public HttpException(int httpStatus, String code, String msg) {
		this(httpStatus, code, msg, (ExceptionMessage) null);
	}

	public HttpException(String code, String msg) {
		this(defaultHttpStatus, code, msg);
	}

	public HttpException(String msg) {
		this(defaultHttpStatus, JahhanErrorCode.UNKNOW_ERROR, msg);
	}

	public HttpException(int httpStatus, String code, String msg, Throwable exception) {
		super(msg, exception);
		this.exceptionMessage = new HttpExceptionMessage();
		if (null != exception) {
			ExceptionMessage cause = new ExceptionMessage();
			cause.setMessage(exception.getMessage());
			this.exceptionMessage.setCause(cause);
		}
		((HttpExceptionMessage) this.exceptionMessage).setHttpStatus(httpStatus);
		this.exceptionMessage.setCode(code);
		this.exceptionMessage.setMessage(msg);
		this.exceptionMessage.setService(BaseConfiguration.SERVICE);
		BaseThreadVariable baseVariable = (BaseThreadVariable) BaseThreadVariable.getThreadVariable("base");
		if (null != baseVariable) {
			this.exceptionMessage.setRequestId(baseVariable.getRequestId());
			this.exceptionMessage.setChainId(baseVariable.getChainId());
		}
		this.exceptionMessage.setHost(LocalIpUtils.getFirstIp());
		this.exceptionMessage.setThreadId(Thread.currentThread().getId());
		this.exceptionMessage.setThreadName(Thread.currentThread().getName());
		this.exceptionMessage.setTime(new Date());

	}

	public HttpException(String code, String msg, Throwable exception) {
		this(defaultHttpStatus, code, msg, exception);
	}

	public HttpException(String msg, Throwable exception) {
		this(defaultHttpStatus, JahhanErrorCode.UNKNOW_ERROR, msg, exception);
	}

	public HttpException(Throwable exception) {
		this(defaultHttpStatus, JahhanErrorCode.UNKNOW_ERROR, exception.getMessage(), exception.getCause());
	}

	public static void throwException(String code, String msg, ExceptionMessage cause) {
		throw new HttpException(code, msg, cause);
	}

	public static void throwException(String code, String msg) {
		throw new HttpException(code, msg);
	}

	public static void throwException(String code, String msg, Throwable exception) {
		throw new HttpException(code, msg, exception);
	}

	public static void throwException(int httpStatus, String code, String msg, ExceptionMessage cause) {
		throw new HttpException(httpStatus, code, msg, cause);
	}

	public static void throwException(int httpStatus, String code, String msg) {
		throw new HttpException(httpStatus, code, msg);
	}

	public static void throwException(int httpStatus, String code, String msg, Throwable exception) {
		throw new HttpException(httpStatus, code, msg, exception);
	}

}
