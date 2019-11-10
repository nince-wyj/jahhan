package net.jahhan.common.extension.exception;

public class NoRollBackException extends JahhanException {

	private static final long serialVersionUID = -3297153548445915405L;

	protected NoRollBackException(String code, String msg) {
		super(code, msg);
	}

	protected NoRollBackException(String code, String msg, ExceptionMessage cause) {
		super(code, msg, cause);
	}

	protected NoRollBackException(String code, String msg, Throwable exception) {
		super(code, msg, exception);
	}
}
