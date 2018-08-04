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
package net.jahhan.extension.invokerListener;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.listener.InvokerListenerAdapter;
import com.frameworkx.annotation.Activate;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.exception.JahhanException;

/**
 * DeprecatedProtocolFilter
 * 
 * @author william.liangf
 */
@Activate(Constants.DEPRECATED_KEY)
@Extension("deprecated")
@Singleton
@Slf4j
public class DeprecatedInvokerListener extends InvokerListenerAdapter {

    public void referred(Invoker<?> invoker) throws JahhanException {
        if (invoker.getUrl().getParameter(Constants.DEPRECATED_KEY, false)) {
            log.error("The service " + invoker.getInterface().getName() + " is DEPRECATED! Declare from " + invoker.getUrl());
        }
    }

}