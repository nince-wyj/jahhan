package net.jahhan.common.extension.context;

/**
 * 线程变量注册与获取
 */
public class ThreadLocalUtil<T> {
	private final ThreadLocal<T> threadLocal = new ThreadLocal<T>();

	public void openThreadLocal(T t) {
		if (null == threadLocal.get()) {
			threadLocal.set(t);
		}
	}

	public T getValue() {
		return threadLocal.get();
	}

	public void closeThreadLocal() {
		threadLocal.remove();
	}
}
