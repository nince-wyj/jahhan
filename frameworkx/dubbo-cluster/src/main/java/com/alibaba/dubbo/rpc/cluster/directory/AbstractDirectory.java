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
package com.alibaba.dubbo.rpc.cluster.directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.router.MockInvokersSelector;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.RouterFactory;

/**
 * 增加router的Directory
 * 
 * @author chao.liuc
 */
@Slf4j
public abstract class AbstractDirectory<T> implements Directory<T> {

    private final URL url ;
    
    private volatile boolean destroyed = false;

    private volatile URL consumerUrl ;
    
	private volatile List<Router> routers;
    
    public AbstractDirectory(URL url) {
        this(url, null);
    }
    
    public AbstractDirectory(URL url, List<Router> routers) {
    	this(url, url, routers);
    }
    
    public AbstractDirectory(URL url, URL consumerUrl, List<Router> routers) {
        if (url == null)
            throw new IllegalArgumentException("url == null");
        this.url = url;
        this.consumerUrl = consumerUrl;
        setRouters(routers);
    }
    
    public List<Invoker<T>> list(Invocation invocation) throws JahhanException {
        if (destroyed){
            throw new JahhanException("Directory already destroyed .url: "+ getUrl());
        }
        List<Invoker<T>> invokers = doList(invocation);
        List<Router> localRouters = this.routers; // local reference
        if (localRouters != null && localRouters.size() > 0) {
            for (Router router: localRouters){
                try {
                    if (router.getUrl() == null || router.getUrl().getParameter(Constants.RUNTIME_KEY, true)) {
                        invokers = router.route(invokers, getConsumerUrl(), invocation);
                    }
                } catch (Throwable t) {
                    log.error("Failed to execute router: " + getUrl() + ", cause: " + t.getMessage(), t);
                }
            }
        }
        return invokers;
    }
    
    public URL getUrl() {
        return url;
    }
    
    public List<Router> getRouters(){
        return routers;
    }

	public URL getConsumerUrl() {
		return consumerUrl;
	}

	public void setConsumerUrl(URL consumerUrl) {
		this.consumerUrl = consumerUrl;
	}

    protected void setRouters(List<Router> routers){
        // copy list
        routers = routers == null ? new  ArrayList<Router>() : new ArrayList<Router>(routers);
        // append url router
    	String routerkey = url.getParameter(Constants.ROUTER_KEY);
        if (routerkey != null && routerkey.length() > 0) {
            RouterFactory routerFactory = ExtensionExtendUtil.getExtension(RouterFactory.class, routerkey);
            routers.add(routerFactory.getRouter(url));
        }
        // append mock invoker selector
        routers.add(new MockInvokersSelector());
        Collections.sort(routers);
    	this.routers = routers;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void destroy(){
        destroyed = true;
    }

    protected abstract List<Invoker<T>> doList(Invocation invocation) throws JahhanException ;

}