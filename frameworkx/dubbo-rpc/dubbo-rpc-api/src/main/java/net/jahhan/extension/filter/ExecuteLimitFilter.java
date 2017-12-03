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

import java.util.concurrent.Semaphore;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcStatus;
import com.frameworkx.annotation.Activate;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.Filter;

/**
 * ThreadLimitInvokerFilter
 * 
 * @author william.liangf
 */
@Activate(group = Constants.PROVIDER, value = Constants.EXECUTES_KEY)
@Extension("executelimit")
@Singleton
public class ExecuteLimitFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws JahhanException {
		URL url = invoker.getUrl();
		String methodName = invocation.getMethodName();
		Semaphore executesLimit = null;
		boolean acquireResult = false;
		int max = url.getMethodParameter(methodName, Constants.EXECUTES_KEY, 0);
		if (max > 0) {
			RpcStatus count = RpcStatus.getStatus(url, invocation.getMethodName());
			// if (count.getActive() >= max) {
			/**
			 * http://manzhizhen.iteye.com/blog/2386408 通过信号量来做并发控制（即限制能使用的线程数量）
			 * 2017-08-21 yizhenqiang
			 */
			executesLimit = count.getSemaphore(max);
			if (executesLimit != null && !(acquireResult = executesLimit.tryAcquire())) {
				throw new JahhanException("Failed to invoke method " + invocation.getMethodName() + " in provider "
						+ url + ", cause: The service using threads greater than <dubbo:service executes=\"" + max
						+ "\" /> limited.");
			}
		}
		long begin = System.currentTimeMillis();
		boolean isSuccess = true;
		RpcStatus.beginCount(url, methodName);
		try {
			Result result = invoker.invoke(invocation);
			return result;
		} catch (Throwable t) {
			isSuccess = false;
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else {
				throw new JahhanException("unexpected exception when ExecuteLimitFilter", t);
			}
		} finally {
			RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, isSuccess);
			if (acquireResult) {
				executesLimit.release();
			}
		}
	}

}