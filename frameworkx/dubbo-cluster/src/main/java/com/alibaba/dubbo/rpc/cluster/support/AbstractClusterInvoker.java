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
package com.alibaba.dubbo.rpc.cluster.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.frameworkx.annotation.DubboInterface;
import com.frameworkx.cache.ClusterMessageHolder;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;
import com.frameworkx.constant.enumeration.RPCTypeEnum;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.spi.LoadBalance;

/**
 * AbstractClusterInvoker
 * 
 * @author william.liangf
 * @author chao.liuc
 */
@Slf4j
public abstract class AbstractClusterInvoker<T> implements Invoker<T> {

	protected final Directory<T> directory;

	protected final boolean availablecheck;

	private AtomicBoolean destroyed = new AtomicBoolean(false);

	private volatile Invoker<T> stickyInvoker = null;

	public AbstractClusterInvoker(Directory<T> directory) {
		this(directory, directory.getUrl());
	}

	public AbstractClusterInvoker(Directory<T> directory, URL url) {
		if (directory == null)
			throw new IllegalArgumentException("service directory == null");

		this.directory = directory;
		// sticky 需要检测 avaliablecheck
		this.availablecheck = url.getParameter(Constants.CLUSTER_AVAILABLE_CHECK_KEY,
				Constants.DEFAULT_CLUSTER_AVAILABLE_CHECK);
	}

	public Class<T> getInterface() {
		return directory.getInterface();
	}

	public URL getUrl() {
		return directory.getUrl();
	}

	public boolean isAvailable() {
		Invoker<T> invoker = stickyInvoker;
		if (invoker != null) {
			return invoker.isAvailable();
		}
		return directory.isAvailable();
	}

	public void destroy() {
		if (destroyed.compareAndSet(false, true)) {
			directory.destroy();
		}
	}

	/**
	 * 使用loadbalance选择invoker.</br>
	 * a)先lb选择，如果在selected列表中 或者 不可用且做检验时，进入下一步(重选),否则直接返回</br>
	 * b)重选验证规则：selected > available .保证重选出的结果尽量不在select中，并且是可用的
	 * 
	 * @param availablecheck
	 *            如果设置true，在选择的时候先选invoker.available == true
	 * @param selected
	 *            已选过的invoker.注意：输入保证不重复
	 * 
	 */
	protected Invoker<T> select(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers,
			List<Invoker<T>> selected) throws JahhanException {
		if (invokers == null || invokers.size() == 0)
			return null;
		String methodName = invocation == null ? "" : invocation.getMethodName();

		boolean sticky = invokers.get(0).getUrl().getMethodParameter(methodName, Constants.CLUSTER_STICKY_KEY,
				Constants.DEFAULT_CLUSTER_STICKY);
		{
			// ignore overloaded method
			if (stickyInvoker != null && !invokers.contains(stickyInvoker)) {
				stickyInvoker = null;
			}
			// ignore cucurrent problem
			if (sticky && stickyInvoker != null && (selected == null || !selected.contains(stickyInvoker))) {
				if (availablecheck && stickyInvoker.isAvailable()) {
					return stickyInvoker;
				}
			}
		}
		Invoker<T> invoker = doselect(loadbalance, invocation, invokers, selected);

		if (sticky) {
			stickyInvoker = invoker;
		}
		return invoker;
	}

	private Invoker<T> doselect(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers,
			List<Invoker<T>> selected) throws JahhanException {
		if (invokers == null || invokers.size() == 0)
			return null;
		if (invokers.size() == 1)
			return invokers.get(0);
		// 如果只有两个invoker，退化成轮循
		if (invokers.size() == 2 && selected != null && selected.size() > 0) {
			return selected.get(0) == invokers.get(0) ? invokers.get(1) : invokers.get(0);
		}
		Invoker<T> invoker = loadbalance.select(invokers, getUrl(), invocation);

		// 如果 selected中包含（优先判断） 或者 不可用&&availablecheck=true 则重试.
		if ((selected != null && selected.contains(invoker))
				|| (!invoker.isAvailable() && getUrl() != null && availablecheck)) {
			try {
				Invoker<T> rinvoker = reselect(loadbalance, invocation, invokers, selected, availablecheck);
				if (rinvoker != null) {
					invoker = rinvoker;
				} else {
					// 看下第一次选的位置，如果不是最后，选+1位置.
					int index = invokers.indexOf(invoker);
					try {
						// 最后在避免碰撞
						invoker = index < invokers.size() - 1 ? invokers.get(index + 1) : invoker;
					} catch (Exception e) {
						log.warn(e.getMessage() + " may because invokers list dynamic change, ignore.", e);
					}
				}
			} catch (Throwable t) {
				log.error("clustor relselect fail reason is :" + t.getMessage()
						+ " if can not slove ,you can set cluster.availablecheck=false in url", t);
			}
		}
		return invoker;
	}

	/**
	 * 重选，先从非selected的列表中选择，没有在从selected列表中选择.
	 * 
	 * @param loadbalance
	 * @param invocation
	 * @param invokers
	 * @param selected
	 * @return
	 * @throws JahhanException
	 */
	private Invoker<T> reselect(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers,
			List<Invoker<T>> selected, boolean availablecheck) throws JahhanException {

		// 预先分配一个，这个列表是一定会用到的.
		List<Invoker<T>> reselectInvokers = new ArrayList<Invoker<T>>(
				invokers.size() > 1 ? (invokers.size() - 1) : invokers.size());

		// 先从非select中选
		if (availablecheck) { // 选isAvailable 的非select
			for (Invoker<T> invoker : invokers) {
				if (invoker.isAvailable()) {
					if (selected == null || !selected.contains(invoker)) {
						reselectInvokers.add(invoker);
					}
				}
			}
			if (reselectInvokers.size() > 0) {
				return loadbalance.select(reselectInvokers, getUrl(), invocation);
			}
		} else { // 选全部非select
			for (Invoker<T> invoker : invokers) {
				if (selected == null || !selected.contains(invoker)) {
					reselectInvokers.add(invoker);
				}
			}
			if (reselectInvokers.size() > 0) {
				return loadbalance.select(reselectInvokers, getUrl(), invocation);
			}
		}
		// 最后从select中选可用的.
		{
			if (selected != null) {
				for (Invoker<T> invoker : selected) {
					if ((invoker.isAvailable()) // 优先选available
							&& !reselectInvokers.contains(invoker)) {
						reselectInvokers.add(invoker);
					}
				}
			}
			if (reselectInvokers.size() > 0) {
				return loadbalance.select(reselectInvokers, getUrl(), invocation);
			}
		}
		return null;
	}

	public Result invoke(final Invocation invocation) throws JahhanException {

		checkWheatherDestoyed();

		LoadBalance loadbalance;

		List<Invoker<T>> invokers = list(invocation);
		invokers = direct(invokers, invocation);
		if (invokers != null && invokers.size() > 0) {
			loadbalance = ExtensionExtendUtil.getExtension(LoadBalance.class,
					invokers.get(0).getUrl().getMethodParameter(invocation.getMethodName(), Constants.LOADBALANCE_KEY,
							Constants.DEFAULT_LOADBALANCE));
		} else {
			loadbalance = ExtensionExtendUtil.getExtension(LoadBalance.class, Constants.DEFAULT_LOADBALANCE);
		}
		RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);
		return doInvoke(invocation, invokers, loadbalance);
	}

	private List<Invoker<T>> direct(List<Invoker<T>> invokers, Invocation invocation) throws JahhanException {
		List<Invoker<T>> filterInvokers = new ArrayList<>();
		ClusterMessageHolder instance = BaseContext.CTX.getInjector().getInstance(ClusterMessageHolder.class);
		Class<T> invokeInterface = invokers.get(0).getInterface();
		DubboInterface dubboInterface = invokeInterface.getAnnotation(DubboInterface.class);
		if (null != dubboInterface && dubboInterface.rpcType().equals(RPCTypeEnum.DIRECT)) {
			if (invocation.getParameterTypes()[0].equals(String.class)) {
				String target = (String) invocation.getArguments()[0];

				if (null != target) {
					Map<String, String> map = instance.getServiceMap().get(target);
					String hosts = map.get(target);
					if (null != hosts) {
						String[] hostArray = hosts.split(",");
						for (String urlString : hostArray) {
							URL url = URL.valueOf(urlString);
							for (Invoker<T> invoker : invokers) {
								URL invokerUrl = invoker.getUrl();
								String uri = invokerUrl.getHost() + ":" + invokerUrl.getPort();

								if (url.getHost().equals(invokerUrl.getHost())
										&& url.getPort() == invokerUrl.getPort()) {
									log.debug("直接推送：" + uri);
									RpcContext.getContext().setAttachment("direct", BaseContext.CTX.getNode().getNodeId());
									filterInvokers.add(invoker);
									invocation.getArguments()[0] = BaseContext.CTX.getNode().getNodeId();
									return filterInvokers;
								}
							}
						}
					}
				}
				throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR,
						"Failed to invoke the method " + invocation.getMethodName() + " in the service "
								+ getInterface().getName() + ". unkown host:" + target
								+ " or target application stop! ");
			}
			throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "error argument,need host target!!");
		}
		return invokers;
	}

	protected void checkWheatherDestoyed() {
		if (destroyed.get()) {
			throw new JahhanException("Rpc cluster invoker for " + getInterface() + " on consumer "
					+ NetUtils.getLocalHost() + " use dubbo version " + Version.getVersion()
					+ " is now destroyed! Can not invoke any more.");
		}
	}

	@Override
	public String toString() {
		return getInterface() + " -> " + getUrl().toString();
	}

	protected void checkInvokers(List<Invoker<T>> invokers, Invocation invocation) {
		if (invokers == null || invokers.size() == 0) {
			throw new JahhanException("Failed to invoke the method " + invocation.getMethodName()
					+ " in the service " + getInterface().getName() + ". No provider available for the service "
					+ directory.getUrl().getServiceKey() + " from registry " + directory.getUrl().getAddress()
					+ " on the consumer " + NetUtils.getLocalHost() + " using the dubbo version " + Version.getVersion()
					+ ". Please check if the providers have been started and registered.");
		}
	}

	protected abstract Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance)
			throws JahhanException;

	protected List<Invoker<T>> list(Invocation invocation) throws JahhanException {
		List<Invoker<T>> invokers = directory.list(invocation);
		return invokers;
	}
}