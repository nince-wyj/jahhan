package net.jahhan.exception;

public class NoRollBackException extends JahhanException {

	private static final long serialVersionUID = -3297153548445915405L;

	protected NoRollBackException(int httpStatus, int code, String msg) {
		super(httpStatus, code, msg);
	}

	protected NoRollBackException(int httpStatus, int code, String msg, ExceptionMessage cause) {
		super(httpStatus, code, msg, cause);
	}

	protected NoRollBackException(int httpStatus, int code, String msg, Throwable exception) {
		super(httpStatus, code, msg, exception);
	}

	protected NoRollBackException(int code, String msg) {
		super(code, msg);
	}

	protected NoRollBackException(int code, String msg, ExceptionMessage cause) {
		super(code, msg, cause);
	}

	protected NoRollBackException(int code, String msg, Throwable exception) {
		super(code, msg, exception);
	}
}
