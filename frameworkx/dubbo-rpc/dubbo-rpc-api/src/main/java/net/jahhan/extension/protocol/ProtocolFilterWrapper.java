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
package net.jahhan.extension.protocol;

import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.api.Wrapper;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.spi.Filter;
import net.jahhan.spi.Protocol;

/**
 * ListenerProtocol
 * 
 * @author william.liangf
 */
@Extension("filter")
public class ProtocolFilterWrapper extends Wrapper<Protocol> implements Protocol {

	public int getDefaultPort() {
		return wrapper.getDefaultPort();
	}

	public <T> Exporter<T> export(Invoker<T> invoker) throws JahhanException {
		if (Constants.REGISTRY_PROTOCOL.equals(invoker.getUrl().getProtocol())) {
			return wrapper.export(invoker);
		}
		return wrapper.export(buildInvokerChain(invoker, Constants.SERVICE_FILTER_KEY, Constants.PROVIDER));
	}

	public <T> Invoker<T> refer(Class<T> type, URL url) throws JahhanException {
		if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
			return wrapper.refer(type, url);
		}
		return buildInvokerChain(wrapper.refer(type, url), Constants.REFERENCE_FILTER_KEY, Constants.CONSUMER);
	}

	public void destroy() {
		wrapper.destroy();
	}

	private static <T> Invoker<T> buildInvokerChain(final Invoker<T> invoker, String key, String group) {
		Invoker<T> last = invoker;
		List<Filter> filters = ExtensionExtendUtil.getActivateExtension(Filter.class, invoker.getUrl(), key, group);
		if (filters.size() > 0) {
			for (int i = filters.size() - 1; i >= 0; i--) {
				final Filter filter = filters.get(i);
				final Invoker<T> next = last;
				last = new Invoker<T>() {

					public Class<T> getInterface() {
						return invoker.getInterface();
					}

					public URL getUrl() {
						return invoker.getUrl();
					}

					public boolean isAvailable() {
						return invoker.isAvailable();
					}

					public Result invoke(Invocation invocation) throws JahhanException {
						return filter.invoke(next, invocation);
					}

					public void destroy() {
						invoker.destroy();
					}

					@Override
					public String toString() {
						return invoker.toString();
					}
				};
			}
		}
		return last;
	}

}