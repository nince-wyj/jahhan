/*
 * Copyright 1999-2012 Alibaba Group.
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
package com.alibaba.dubbo.rpc.cluster.support;

import java.util.List;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.cluster.Directory;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.LoadBalance;

/**
 * BroadcastClusterInvoker
 * 
 * @author william.liangf
 */
@Slf4j
public class BroadcastClusterInvoker<T> extends AbstractClusterInvoker<T> {
    
    public BroadcastClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Result doInvoke(final Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws JahhanException {
        checkInvokers(invokers, invocation);
        RpcContext.getContext().setInvokers((List)invokers);
        JahhanException exception = null;
        Result result = null;
        for (Invoker<T> invoker: invokers) {
            try {
                result = invoker.invoke(invocation);
            } catch (JahhanException e) {
                exception = e;
                log.warn(e.getMessage(), e);
            } catch (Throwable e) {
                exception = new JahhanException(e.getMessage(), e);
                log.warn(e.getMessage(), e);
            }
        }
        if (exception != null) {
            throw exception;
        }
        return result;
    }

}