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

package com.alibaba.dubbo.rpc.protocol;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.frameworkx.annotation.Adaptive;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.spi.ProxyFactory;

/**
 * AbstractProxyProtocol
 * 
 * @author william.liangf
 */
@Slf4j
public abstract class AbstractProxyProtocol extends AbstractProtocol {

    private final List<Class<?>> JahhanExceptionList = new CopyOnWriteArrayList<Class<?>>();;
    @Inject
	@Adaptive
    private ProxyFactory proxyFactory;

    public AbstractProxyProtocol() {
    }

    public AbstractProxyProtocol(Class<?>... exceptions) {
        for (Class<?> exception : exceptions) {
            addJahhanException(exception);
        }
    }

    public void addJahhanException(Class<?> exception) {
        this.JahhanExceptionList.add(exception);
    }

    @SuppressWarnings("unchecked")
	public <T> Exporter<T> export(final Invoker<T> invoker) throws JahhanException {
        final String uri = serviceKey(invoker.getUrl());
        Exporter<T> exporter = (Exporter<T>) exporterMap.get(uri);
        if (exporter != null) {
        	return exporter;
        }
        final Runnable runnable = doExport(proxyFactory.getProxy(invoker), invoker.getInterface(), invoker.getUrl());
        exporter = new AbstractExporter<T>(invoker) {
            public void unexport() {
                super.unexport();
                exporterMap.remove(uri);
                if (runnable != null) {
                    try {
                        runnable.run();
                    } catch (Throwable t) {
                        log.warn(t.getMessage(), t);
                    }
                }
            }
        };
        exporterMap.put(uri, exporter);
        return exporter;
    }

    public <T> Invoker<T> refer(final Class<T> type, final URL url) throws JahhanException {
        final Invoker<T> tagert = proxyFactory.getInvoker(doRefer(type, url), type, url);
        Invoker<T> invoker = new AbstractInvoker<T>(type, url) {
            @Override
            protected Result doInvoke(Invocation invocation) throws Throwable {
                try {
                    Result result = tagert.invoke(invocation);
                    Throwable e = result.getException();
                    if (e != null) {
                        for (Class<?> JahhanException : JahhanExceptionList) {
                            if (JahhanException.isAssignableFrom(e.getClass())) {
                                throw getJahhanException(type, url, invocation, e);
                            }
                        }
                    }
                    return result;
                } catch (JahhanException e) {
                    if (e.getCode() == JahhanErrorCode.UNKNOW_ERROR) {
                        e.setCode(getErrorCode(e.getCause()));
                    }
                    throw e;
                } catch (Throwable e) {
                    throw getJahhanException(type, url, invocation, e);
                }
            }
        };
        invokers.add(invoker);
        return invoker;
    }

    protected JahhanException getJahhanException(Class<?> type, URL url, Invocation invocation, Throwable e) {
        JahhanException re = new JahhanException("Failed to invoke remote service: " + type + ", method: "
                + invocation.getMethodName() + ", cause: " + e.getMessage(), e);
        re.setCode(getErrorCode(e));
        return re;
    }

    protected String getErrorCode(Throwable e) {
        return JahhanErrorCode.UNKNOW_ERROR;
    }

    protected abstract <T> Runnable doExport(T impl, Class<T> type, URL url) throws JahhanException;

    protected abstract <T> T doRefer(Class<T> type, URL url) throws JahhanException;

}
