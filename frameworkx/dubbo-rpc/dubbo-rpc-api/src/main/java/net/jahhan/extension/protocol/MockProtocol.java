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

import javax.inject.Singleton;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;
import com.alibaba.dubbo.rpc.support.MockInvoker;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.exception.JahhanException;

/**
 * MockProtocol 用于在consumer side 通过url及类型生成一个mockInvoker
 * @author chao.liuc
 *
 */
@Extension("mock")
@Singleton
final public class MockProtocol extends AbstractProtocol {

	public int getDefaultPort() {
		return 0;
	}

	public <T> Exporter<T> export(Invoker<T> invoker) throws JahhanException {
		throw new UnsupportedOperationException();
	}

	public <T> Invoker<T> refer(Class<T> type, URL url) throws JahhanException {
		return new MockInvoker<T>(url);
	}
}
