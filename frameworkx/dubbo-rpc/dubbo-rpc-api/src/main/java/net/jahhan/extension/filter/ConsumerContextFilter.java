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

import java.util.UUID;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.frameworkx.annotation.Activate;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.context.BaseVariable;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.Filter;

/**
 * ConsumerContextInvokerFilter
 * 
 * @author william.liangf
 */
@Activate(group = Constants.CONSUMER, order = -10000)
@Extension("consumercontext")
@Singleton
public class ConsumerContextFilter implements Filter {

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws JahhanException {
    	String host = ConfigUtils.getProperty("dubbo.protocol.host",NetUtils.getLocalHost());
		RpcContext.getContext()
                .setInvoker(invoker)
                .setInvocation(invocation)
                .setLocalAddress(host, 0)
                .setRemoteAddress(invoker.getUrl().getHost(), 
                                  invoker.getUrl().getPort());
		RpcContext.getContext().setAttachment("request_id", UUID.randomUUID().toString());
		RpcContext.getContext().setAttachment("chain_id", BaseVariable.getBaseVariable().getChainId());
        if (invocation instanceof RpcInvocation) {
            ((RpcInvocation)invocation).setInvoker(invoker);
        }
        try {
            return invoker.invoke(invocation);
        } finally {
            RpcContext.getContext().clearAttachments();
        }
    }

}