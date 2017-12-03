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
package net.jahhan.extension.cluster;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.support.wrapper.MockClusterInvoker;

import net.jahhan.api.Wrapper;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.Cluster;

/**
 * mock impl
 * 
 * @author chao.liuc
 * 
 */
@Extension("mock")
public class MockClusterWrapper extends Wrapper<Cluster> implements Cluster {


	public <T> Invoker<T> join(Directory<T> directory) throws JahhanException {
		return new MockClusterInvoker<T>(directory,
				this.wrapper.join(directory));
	}

}
