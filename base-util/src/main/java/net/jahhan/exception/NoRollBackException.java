package net.jahhan.exception;

public class NoRollBackException extends RuntimeException {

    private static final long serialVersionUID = -3297153548445915405L;

    private final int code;

    private final String msg;

    public NoRollBackException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public NoRollBackException(int code, String msg, Throwable exception) {
        super(msg, exception);
        this.code = code;
        this.msg = msg;
    }

    public static void throwException(int code, String msg) {
        throw new NoRollBackException(code, msg);
    }

    public static void throwException(int code, String msg, Throwable exception) {
        throw new NoRollBackException(code, msg, exception);
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return msg;
    }

}
