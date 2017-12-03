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

import java.util.Collections;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.listener.ListenerExporterWrapper;
import com.alibaba.dubbo.rpc.listener.ListenerInvokerWrapper;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import net.jahhan.api.Wrapper;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.ExporterListener;
import net.jahhan.spi.InvokerListener;
import net.jahhan.spi.Protocol;

/**
 * ListenerProtocol
 * 
 * @author william.liangf
 */
@Extension("listener")
public class ProtocolListenerWrapper extends Wrapper<Protocol> implements Protocol {

	public int getDefaultPort() {
		return wrapper.getDefaultPort();
	}

	public <T> Exporter<T> export(Invoker<T> invoker) throws JahhanException {
		if (Constants.REGISTRY_PROTOCOL.equals(invoker.getUrl().getProtocol())) {
			return wrapper.export(invoker);
		}
		return new ListenerExporterWrapper<T>(wrapper.export(invoker), Collections.unmodifiableList(ExtensionExtendUtil
				.getActivateExtension(ExporterListener.class, invoker.getUrl(), Constants.EXPORTER_LISTENER_KEY)));
	}

	public <T> Invoker<T> refer(Class<T> type, URL url) throws JahhanException {
		if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
			return wrapper.refer(type, url);
		}
		return new ListenerInvokerWrapper<T>(wrapper.refer(type, url), Collections.unmodifiableList(
				ExtensionExtendUtil.getActivateExtension(InvokerListener.class, url, Constants.INVOKER_LISTENER_KEY)));
	}

	public void destroy() {
		wrapper.destroy();
	}

}