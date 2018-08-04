package net.jahhan.register.api;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.context.Node;

@Slf4j
public abstract class FailbackRegistry implements Registry {

	private AtomicBoolean destroyed = new AtomicBoolean(false);
	@Setter
	protected Node node;

	@Override
	public void register() {
		boolean regist = false;
		while (!regist) {
			try {
				// 向服务器端发送注册请求
				doRegister();
				regist = true;
			} catch (Exception e) {
				log.error("注册中心连接失败！", e);
			}
		}

	}

	@Override
	public void unregister() {
		if (destroyed.get()) {
			return;
		}
		try {
			// 向服务器端发送取消注册请求
			doUnregister();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		if (destroyed.get()) {
			return;
		}
		if (destroyed.compareAndSet(false, true))
			doDestroy();
	}

	protected abstract void doRegister();

	protected abstract void doUnregister();

	protected abstract void doDestroy();
}