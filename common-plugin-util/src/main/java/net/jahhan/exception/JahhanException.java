package net.jahhan.exception;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.LocalIpUtils;
import net.jahhan.context.BaseVariable;

@Data
@EqualsAndHashCode(callSuper = false)
public class JahhanException extends RuntimeException {

	private static final long serialVersionUID = -3297153548445915405L;
	protected ExceptionMessage exceptionMessage;
	private static int defaultHttpStatus = 500;

	public JahhanException(int httpStatus, int code, String msg, ExceptionMessage cause) {
		super(msg);
		this.exceptionMessage = new ExceptionMessage();
		this.exceptionMessage.setHttpStatus(httpStatus);
		this.exceptionMessage.setCode(code);
		this.exceptionMessage.setMessage(msg);
		this.exceptionMessage.setService(BaseConfiguration.SERVICE);
		BaseVariable baseVariable = BaseVariable.getBaseVariable();
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

	public JahhanException(int code, String msg, ExceptionMessage cause) {
		this(defaultHttpStatus, code, msg, cause);
		this.exceptionMessage.setHttpStatus(cause.getHttpStatus());
	}

	public JahhanException(int httpStatus, int code, String msg) {
		this(httpStatus, code, msg, (ExceptionMessage) null);
	}

	public JahhanException(int code, String msg) {
		this(defaultHttpStatus, code, msg);
	}

	public JahhanException(String msg) {
		this(defaultHttpStatus, JahhanErrorCode.UNKNOW_ERROR, msg);
	}

	public JahhanException(int httpStatus, int code, String msg, Throwable exception) {
		super(msg, exception);
		this.exceptionMessage = new ExceptionMessage();
		if (null != exception) {
			ExceptionMessage cause = new ExceptionMessage();
			cause.setMessage(exception.getMessage());
			this.exceptionMessage.setCause(cause);
		}
		this.exceptionMessage.setHttpStatus(httpStatus);
		this.exceptionMessage.setCode(code);
		this.exceptionMessage.setMessage(msg);
		this.exceptionMessage.setService(BaseConfiguration.SERVICE);
		BaseVariable baseVariable = BaseVariable.getBaseVariable();
		if (null != baseVariable) {
			this.exceptionMessage.setRequestId(baseVariable.getRequestId());
			this.exceptionMessage.setChainId(baseVariable.getChainId());
		}
		this.exceptionMessage.setHost(LocalIpUtils.getFirstIp());
		this.exceptionMessage.setThreadId(Thread.currentThread().getId());
		this.exceptionMessage.setThreadName(Thread.currentThread().getName());
		this.exceptionMessage.setTime(new Date());

	}

	public JahhanException(int code, String msg, Throwable exception) {
		this(defaultHttpStatus, code, msg, exception);
	}

	public JahhanException(String msg, Throwable exception) {
		this(defaultHttpStatus, JahhanErrorCode.UNKNOW_ERROR, msg, exception);
	}

	public JahhanException(Throwable exception) {
		this(defaultHttpStatus, JahhanErrorCode.UNKNOW_ERROR, exception.getMessage(), exception.getCause());
	}

	public JahhanException(int code, Throwable exception) {
		this(defaultHttpStatus, code, exception.getMessage(), exception.getCause());
	}

	public static void throwException(int code, String msg, ExceptionMessage cause) {
		throw new JahhanException(code, msg, cause);
	}

	public static void throwException(int code, String msg) {
		throw new JahhanException(code, msg);
	}

	public static void throwException(int code, String msg, Throwable exception) {
		throw new JahhanException(code, msg, exception);
	}

	public static void throwException(int httpStatus, int code, String msg, ExceptionMessage cause) {
		throw new JahhanException(httpStatus, code, msg, cause);
	}

	public static void throwException(int httpStatus, int code, String msg) {
		throw new JahhanException(httpStatus, code, msg);
	}

	public static void throwException(int httpStatus, int code, String msg, Throwable exception) {
		throw new JahhanException(httpStatus, code, msg, exception);
	}

	public int getCode() {
		return this.exceptionMessage.getCode();
	}

	public void setCode(int code) {
		this.exceptionMessage.setCode(code);
	}

	@Override
	public String getMessage() {
		return exceptionMessage.getMessage();
	}

	public boolean isBiz() {
		return exceptionMessage.getCode() == JahhanErrorCode.BIZ_EXCEPTION;
	}

	public boolean isForbidded() {
		return exceptionMessage.getCode() == JahhanErrorCode.FORBIDDEN_EXCEPTION;
	}

	public boolean isTimeout() {
		return exceptionMessage.getCode() == JahhanErrorCode.TIMEOUT_EXCEPTION;
	}

	public boolean isNetwork() {
		return exceptionMessage.getCode() == JahhanErrorCode.NETWORK_EXCEPTION;
	}

	public boolean isSerialization() {
		return exceptionMessage.getCode() == JahhanErrorCode.SERIALIZATION_EXCEPTION;
	}
}
