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
package net.jahhan.spi;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;

import net.jahhan.common.extension.annotation.SPI;
import net.jahhan.exception.JahhanException;

/**
 * InvokerListener. (SPI, Singleton, ThreadSafe)
 * 
 * @author william.liangf
 */
@SPI
public interface InvokerListener {

	/**
	 * The invoker referred
	 * 
	 * @see net.jahhan.spi.Protocol#refer(Class, URL)
	 * @param invoker
	 * @throws JahhanException
	 */
	void referred(Invoker<?> invoker) throws JahhanException;

	/**
	 * The invoker destroyed.
	 * 
	 * @see com.alibaba.dubbo.rpc.Invoker#destroy()
	 * @param invoker
	 */
	void destroyed(Invoker<?> invoker);

}