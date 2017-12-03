package com.alibaba.dubbo.common.utils;

import java.util.List;

import com.alibaba.dubbo.common.URL;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import net.jahhan.exception.JahhanException;
import net.jahhan.spi.AfterWriteHandler;

public abstract class AfterWriteHandlerHelper {
	private static final AfterWriteHandler afterWriteHandler;

	static {
		List<AfterWriteHandler> afterWriteHandlerList = ExtensionExtendUtil.getActivateExtension(AfterWriteHandler.class,
				new URL(null, null, 0), "", null);
		afterWriteHandler = new AfterWriteHandler() {
			private List<AfterWriteHandler> afterWriteHandlers = afterWriteHandlerList;

			@Override
			public void exec() throws JahhanException {
				if (afterWriteHandlers.size() > 0) {
					for (AfterWriteHandler afterWriteHandler : afterWriteHandlers) {
						afterWriteHandler.exec();
					}
				}
			}
		};
	}

	public static AfterWriteHandler getAllHandler() {
		return afterWriteHandler;
	}
}
