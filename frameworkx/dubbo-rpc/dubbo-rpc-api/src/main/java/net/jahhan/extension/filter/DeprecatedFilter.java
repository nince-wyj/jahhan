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
package net.jahhan.extension.filter;

import java.util.Set;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.frameworkx.annotation.Activate;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.Filter;

/**
 * DeprecatedInvokerFilter
 * 
 * @author william.liangf
 */
@Activate(group = Constants.CONSUMER, value = Constants.DEPRECATED_KEY)
@Extension("deprecated")
@Singleton
@Slf4j
public class DeprecatedFilter implements Filter {

    private static final Set<String> logged = new ConcurrentHashSet<String>();

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws JahhanException {
        String key = invoker.getInterface().getName() + "." + invocation.getMethodName();
        if (! logged.contains(key)) {
            logged.add(key);
            if (invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.DEPRECATED_KEY, false)) {
                log.error("The service method " + invoker.getInterface().getName() + "." + getMethodSignature(invocation) + " is DEPRECATED! Declare from " + invoker.getUrl());
            }
        }
        return invoker.invoke(invocation);
    }
    
    private String getMethodSignature(Invocation invocation) {
        StringBuilder buf = new StringBuilder(invocation.getMethodName());
        buf.append("(");
        Class<?>[] types = invocation.getParameterTypes();
        if (types != null && types.length > 0) {
            boolean first = true;
            for (Class<?> type : types) {
                if (first) {
                    first = false;
                } else {
                    buf.append(", ");
                }
                buf.append(type.getSimpleName());
            }
        }
        buf.append(")");
        return buf.toString();
    }

}