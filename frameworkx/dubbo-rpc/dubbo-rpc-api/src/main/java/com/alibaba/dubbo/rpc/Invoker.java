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
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.Node;

import net.jahhan.exception.JahhanException;

/**
 * Invoker. (API/SPI, Prototype, ThreadSafe)
 * 
 * @see net.jahhan.spi.Protocol#refer(Class, com.alibaba.dubbo.common.compiler.support.URL)
 * @see net.jahhan.spi.InvokerListener
 * @see com.alibaba.dubbo.rpc.protocol.AbstractInvoker
 * @author william.liangf
 */
public interface Invoker<T> extends Node {

    /**
     * get service interface.
     * 
     * @return service interface.
     */
    Class<T> getInterface();

    /**
     * invoke.
     * 
     * @param invocation
     * @return result
     * @throws JahhanException
     */
    Result invoke(Invocation invocation) throws JahhanException;

}