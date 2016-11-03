package net.jahhan.exception;

public class FrameworkException extends RuntimeException {

    private static final long serialVersionUID = -3297153548445915405L;

    private int code;

    private String msg;

    private FrameworkException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    private FrameworkException(int code, String msg, Throwable exception) {
        super(msg, exception);
        this.code = code;
        this.msg = msg;
    }

    public static void throwException(int code, String msg) {
        throw new FrameworkException(code, msg);
    }

    public static void throwException(int code, String msg, Throwable exception) {
        throw new FrameworkException(code, msg, exception);
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return msg;
    }

}
