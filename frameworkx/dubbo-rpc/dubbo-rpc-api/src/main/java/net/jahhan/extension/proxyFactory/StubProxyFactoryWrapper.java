/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jahhan.extension.proxyFactory;

import java.lang.reflect.Constructor;

import javax.inject.Inject;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.frameworkx.annotation.Adaptive;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.utils.StringUtils;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.Protocol;
import net.jahhan.spi.ProxyFactory;

/**
 * StubProxyFactoryWrapper
 * 
 * @author william.liangf
 */
@Extension("stub")
@Slf4j
public class StubProxyFactoryWrapper extends net.jahhan.api.Wrapper<ProxyFactory> implements ProxyFactory {

	@Inject
	@Adaptive
	private Protocol protocol;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T getProxy(Invoker<T> invoker) throws JahhanException {
		T proxy = wrapper.getProxy(invoker);
		if (GenericService.class != invoker.getInterface()) {
			String stub = invoker.getUrl().getParameter(Constants.STUB_KEY,
					invoker.getUrl().getParameter(Constants.LOCAL_KEY));
			if (ConfigUtils.isNotEmpty(stub)) {
				Class<?> serviceType = invoker.getInterface();
				if (ConfigUtils.isDefault(stub)) {
					if (invoker.getUrl().hasParameter(Constants.STUB_KEY)) {
						stub = serviceType.getName() + "Stub";
					} else {
						stub = serviceType.getName() + "Local";
					}
				}
				try {
					Class<?> stubClass = ReflectUtils.forName(stub);
					if (!serviceType.isAssignableFrom(stubClass)) {
						throw new IllegalStateException("The stub implemention class " + stubClass.getName()
								+ " not implement interface " + serviceType.getName());
					}
					try {
						Constructor<?> constructor = ReflectUtils.findConstructor(stubClass, serviceType);
						proxy = (T) constructor.newInstance(new Object[] { proxy });
						// export stub service
						URL url = invoker.getUrl();
						if (url.getParameter(Constants.STUB_EVENT_KEY, Constants.DEFAULT_STUB_EVENT)) {
							url = url.addParameter(Constants.STUB_EVENT_METHODS_KEY, StringUtils
									.join(Wrapper.getWrapper(proxy.getClass()).getDeclaredMethodNames(), ","));
							url = url.addParameter(Constants.IS_SERVER_KEY, Boolean.FALSE.toString());
							try {
								export(proxy, (Class) invoker.getInterface(), url);
							} catch (Exception e) {
								log.error("export a stub service error.", e);
							}
						}
					} catch (NoSuchMethodException e) {
						throw new IllegalStateException("No such constructor \"public " + stubClass.getSimpleName()
								+ "(" + serviceType.getName() + ")\" in stub implemention class " + stubClass.getName(),
								e);
					}
				} catch (Throwable t) {
					log.error("Failed to create stub implemention class " + stub + " in consumer "
							+ NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion() + ", cause: "
							+ t.getMessage(), t);
					// ignore
				}
			}
		}
		return proxy;
	}

	public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws JahhanException {
		return wrapper.getInvoker(proxy, type, url);
	}

	private <T> Exporter<T> export(T instance, Class<T> type, URL url) {
		return protocol.export(wrapper.getInvoker(instance, type, url));
	}

}