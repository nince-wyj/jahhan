package net.jahhan.jedis;

public class RedisException extends RuntimeException {

    private static final long serialVersionUID = 3317154016749700649L;

    public RedisException(final Throwable cause) {
        super(cause);
    }

    public RedisException(String msg) {
        super(msg);
    }

    public RedisException(String msg, Throwable e) {
        super(msg, e);
    }
}
