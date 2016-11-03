package net.jahhan.guiceutils;

/**
 * 线程变量注册与获取
 */
public class ThreadLocalUtil<T> {
    private final ThreadLocal<T> threadLocal = new ThreadLocal<T>();

    public void openThreadLocal(T t) {
        threadLocal.set(t);
    }

    public T getValue() {
        return threadLocal.get();
    }

    public void closeThreadLocal() {
        threadLocal.remove();
    }
}
