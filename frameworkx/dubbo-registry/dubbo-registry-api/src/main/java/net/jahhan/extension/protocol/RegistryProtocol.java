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
package net.jahhan.extension.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryService;
import com.alibaba.dubbo.registry.integration.RegistryDirectory;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.Configurator;
import com.alibaba.dubbo.rpc.protocol.InvokerWrapper;
import com.frameworkx.annotation.Adaptive;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.StringUtils;
import net.jahhan.spi.Cluster;
import net.jahhan.spi.Protocol;
import net.jahhan.spi.ProxyFactory;
import net.jahhan.spi.RegistryFactory;

/**
 * RegistryProtocol
 * 
 * @author william.liangf
 * @author chao.liuc
 */
@Extension("registry")
@Singleton
@Slf4j
public class RegistryProtocol implements Protocol {
	@Inject
	private Cluster cluster;

	@Inject
	@Adaptive
	private Protocol protocol;

	@Inject
	@Adaptive
	private RegistryFactory registryFactory;

	@Inject
	@Adaptive
	private ProxyFactory proxyFactory;

	public int getDefaultPort() {
		return 9090;
	}

	private static RegistryProtocol INSTANCE;

	public RegistryProtocol() {
		INSTANCE = this;
	}

	public static RegistryProtocol getRegistryProtocol() {
		if (INSTANCE == null) {
			INSTANCE = (RegistryProtocol) ExtensionExtendUtil.getExtension(Protocol.class, Constants.REGISTRY_PROTOCOL);
		}
		return INSTANCE;
	}

	private final Map<URL, NotifyListener> overrideListeners = new ConcurrentHashMap<URL, NotifyListener>();

	public Map<URL, NotifyListener> getOverrideListeners() {
		return overrideListeners;
	}

	// 用于解决rmi重复暴露端口冲突的问题，已经暴露过的服务不再重新暴露
	// providerurl <--> exporter
	private final Map<String, ExporterChangeableWrapper<?>> bounds = new ConcurrentHashMap<String, ExporterChangeableWrapper<?>>();

	public <T> Exporter<T> export(final Invoker<T> originInvoker) throws JahhanException {
		// export invoker
		final ExporterChangeableWrapper<T> exporter = doLocalExport(originInvoker);
		// registry provider
		final Registry registry = getRegistry(originInvoker);
		URL registingProviderUrl = getRegistedProviderUrl(originInvoker);
		String interfaceName = registingProviderUrl.getParameter("interface");
		if (!interfaceName.endsWith("SwaggerService")) {
			String parasitiferHost = System.getenv("PARASITIFER_HOST");
			boolean realMachine = org.apache.commons.lang3.StringUtils.isEmpty(parasitiferHost);
			if (!realMachine) {
				int mappingPort = NetUtils.getMappingPort(registingProviderUrl.getPort());
				registingProviderUrl = registingProviderUrl.setHost(parasitiferHost);
				registingProviderUrl = registingProviderUrl.setPort(mappingPort);
			}
			registry.register(registingProviderUrl);
		}
		final URL registedProviderUrl = registingProviderUrl;
		// 订阅override数据
		// FIXME
		// 提供者订阅时，会影响同一JVM即暴露服务，又引用同一服务的的场景，因为subscribed以服务名为缓存的key，导致订阅信息覆盖。
		final URL overrideSubscribeUrl = getSubscribedOverrideUrl(registedProviderUrl);
		final OverrideListener overrideSubscribeListener = new OverrideListener(overrideSubscribeUrl, originInvoker);
		overrideListeners.put(overrideSubscribeUrl, overrideSubscribeListener);
		registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener);
		// 保证每次export都返回一个新的exporter实例
		return new Exporter<T>() {
			public Invoker<T> getInvoker() {
				return exporter.getInvoker();
			}

			public void unexport() {
				try {
					exporter.unexport();
				} catch (Throwable t) {
					log.warn(t.getMessage(), t);
				}
				try {
					registry.unregister(registedProviderUrl);
				} catch (Throwable t) {
					log.warn(t.getMessage(), t);
				}
				try {
					overrideListeners.remove(overrideSubscribeUrl);
					registry.unsubscribe(overrideSubscribeUrl, overrideSubscribeListener);
				} catch (Throwable t) {
					log.warn(t.getMessage(), t);
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	private <T> ExporterChangeableWrapper<T> doLocalExport(final Invoker<T> originInvoker) {
		String key = getCacheKey(originInvoker);
		ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
		if (exporter == null) {
			synchronized (bounds) {
				exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
				if (exporter == null) {
					final Invoker<?> invokerDelegete = new InvokerDelegete<T>(originInvoker,
							getProviderUrl(originInvoker));
					exporter = new ExporterChangeableWrapper<T>((Exporter<T>) protocol.export(invokerDelegete),
							originInvoker);
					bounds.put(key, exporter);
				}
			}
		}
		return exporter;
	}

	/**
	 * 对修改了url的invoker重新export
	 * 
	 * @param originInvoker
	 * @param newInvokerUrl
	 */
	@SuppressWarnings("unchecked")
	private <T> void doChangeLocalExport(final Invoker<T> originInvoker, URL newInvokerUrl) {
		String key = getCacheKey(originInvoker);
		final ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
		if (exporter == null) {
			log.warn("", new IllegalStateException("error state, exporter should not be null"));
		} else {
			final Invoker<T> invokerDelegete = new InvokerDelegete<T>(originInvoker, newInvokerUrl);
			exporter.setExporter(protocol.export(invokerDelegete));
		}
	}

	/**
	 * 根据invoker的地址获取registry实例
	 * 
	 * @param originInvoker
	 * @return
	 */
	private Registry getRegistry(final Invoker<?> originInvoker) {
		URL registryUrl = originInvoker.getUrl();
		if (Constants.REGISTRY_PROTOCOL.equals(registryUrl.getProtocol())) {
			String protocol = registryUrl.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_DIRECTORY);
			registryUrl = registryUrl.setProtocol(protocol).removeParameter(Constants.REGISTRY_KEY);
		}
		return registryFactory.getRegistry(registryUrl);
	}

	/**
	 * 返回注册到注册中心的URL，对URL参数进行一次过滤
	 * 
	 * @param originInvoker
	 * @return
	 */
	private URL getRegistedProviderUrl(final Invoker<?> originInvoker) {
		URL providerUrl = getProviderUrl(originInvoker);
		// 注册中心看到的地址
		final URL registedProviderUrl = providerUrl.removeParameters(getFilteredKeys(providerUrl))
				.removeParameter(Constants.MONITOR_KEY);
		return registedProviderUrl;
	}

	private URL getSubscribedOverrideUrl(URL registedProviderUrl) {
		return registedProviderUrl.setProtocol(Constants.PROVIDER_PROTOCOL).addParameters(Constants.CATEGORY_KEY,
				Constants.CONFIGURATORS_CATEGORY, Constants.CHECK_KEY, String.valueOf(false));
	}

	/**
	 * 通过invoker的url 获取 providerUrl的地址
	 * 
	 * @param origininvoker
	 * @return
	 */
	private URL getProviderUrl(final Invoker<?> origininvoker) {
		String export = origininvoker.getUrl().getParameterAndDecoded(Constants.EXPORT_KEY);
		if (export == null || export.length() == 0) {
			throw new IllegalArgumentException("The registry export url is null! registry: " + origininvoker.getUrl());
		}

		URL providerUrl = URL.valueOf(export);
		return providerUrl;
	}

	/**
	 * 获取invoker在bounds中缓存的key
	 * 
	 * @param originInvoker
	 * @return
	 */
	private String getCacheKey(final Invoker<?> originInvoker) {
		URL providerUrl = getProviderUrl(originInvoker);
		String key = providerUrl.removeParameters("dynamic", "enabled").toFullString();
		return key;
	}

	@SuppressWarnings("unchecked")
	public <T> Invoker<T> refer(Class<T> type, URL url) throws JahhanException {
		url = url.setProtocol(url.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_REGISTRY))
				.removeParameter(Constants.REGISTRY_KEY);
		Registry registry = registryFactory.getRegistry(url);
		if (RegistryService.class.equals(type)) {
			return proxyFactory.getInvoker((T) registry, type, url);
		}

		// group="a,b" or group="*"
		Map<String, String> qs = StringUtils.parseQueryString(url.getParameterAndDecoded(Constants.REFER_KEY));
		String group = qs.get(Constants.GROUP_KEY);
		if (group != null && group.length() > 0) {
			if ((Constants.COMMA_SPLIT_PATTERN.split(group)).length > 1 || "*".equals(group)) {
				return doRefer(getMergeableCluster(), registry, type, url);
			}
		}
		return doRefer(cluster, registry, type, url);
	}

	private Cluster getMergeableCluster() {
		return ExtensionExtendUtil.getExtension(Cluster.class, "mergeable");
	}

	private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {
		RegistryDirectory<T> directory = new RegistryDirectory<T>(type, url);
		directory.setRegistry(registry);
		directory.setProtocol(protocol);
		String host = ConfigUtils.getProperty("dubbo.protocol.host", NetUtils.getLocalHost());
		URL subscribeUrl = new URL(Constants.CONSUMER_PROTOCOL, host, 0, type.getName(),
				directory.getUrl().getParameters());
		if (!Constants.ANY_VALUE.equals(url.getServiceInterface()) && url.getParameter(Constants.REGISTER_KEY, true)) {
			registry.register(subscribeUrl.addParameters(Constants.CATEGORY_KEY, Constants.CONSUMERS_CATEGORY,
					Constants.CHECK_KEY, String.valueOf(false)));
		}
		directory.subscribe(subscribeUrl.addParameter(Constants.CATEGORY_KEY, Constants.PROVIDERS_CATEGORY + ","
				+ Constants.CONFIGURATORS_CATEGORY + "," + Constants.ROUTERS_CATEGORY));
		return cluster.join(directory);
	}

	// 过滤URL中不需要输出的参数(以点号开头的)
	private static String[] getFilteredKeys(URL url) {
		Map<String, String> params = url.getParameters();
		if (params != null && !params.isEmpty()) {
			List<String> filteredKeys = new ArrayList<String>();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				if (entry != null && entry.getKey() != null && entry.getKey().startsWith(Constants.HIDE_KEY_PREFIX)) {
					filteredKeys.add(entry.getKey());
				}
			}
			return filteredKeys.toArray(new String[filteredKeys.size()]);
		} else {
			return new String[] {};
		}
	}

	public void destroy() {
		List<Exporter<?>> exporters = new ArrayList<Exporter<?>>(bounds.values());
		for (Exporter<?> exporter : exporters) {
			exporter.unexport();
		}
		bounds.clear();
	}

	/*
	 * 重新export 1.protocol中的exporter destory问题
	 * 1.要求registryprotocol返回的exporter可以正常destroy 2.notify后不需要重新向注册中心注册 3.export
	 * 方法传入的invoker最好能一直作为exporter的invoker.
	 */
	private class OverrideListener implements NotifyListener {

		private final URL subscribeUrl;
		private final Invoker originInvoker;

		public OverrideListener(URL subscribeUrl, Invoker originalInvoker) {
			this.subscribeUrl = subscribeUrl;
			this.originInvoker = originalInvoker;
		}

		/**
		 * @param urls
		 *            已注册信息列表，总不为空，含义同
		 *            {@link com.alibaba.dubbo.registry.RegistryService#lookup(URL)}
		 *            的返回值。
		 */
		public synchronized void notify(List<URL> urls) {
			log.debug("original override urls: " + urls);
			List<URL> matchedUrls = getMatchedUrls(urls, subscribeUrl);
			log.debug("subscribe url: " + subscribeUrl + ", override urls: " + matchedUrls);
			// 没有匹配的
			if (matchedUrls.isEmpty()) {
				return;
			}

			List<Configurator> configurators = RegistryDirectory.toConfigurators(matchedUrls);

			final Invoker<?> invoker;
			if (originInvoker instanceof InvokerDelegete) {
				invoker = ((InvokerDelegete<?>) originInvoker).getInvoker();
			} else {
				invoker = originInvoker;
			}
			// 最原始的invoker
			URL originUrl = RegistryProtocol.this.getProviderUrl(invoker);
			String key = getCacheKey(originInvoker);
			ExporterChangeableWrapper<?> exporter = bounds.get(key);
			if (exporter == null) {
				log.warn("", new IllegalStateException("error state, exporter should not be null"));
				return;
			}
			// 当前的，可能经过了多次merge
			URL currentUrl = exporter.getInvoker().getUrl();
			// 与本次配置merge的
			URL newUrl = getConfigedInvokerUrl(configurators, originUrl);
			if (!currentUrl.equals(newUrl)) {
				RegistryProtocol.this.doChangeLocalExport(originInvoker, newUrl);
				log.info("exported provider url changed, origin url: " + originUrl + ", old export url: " + currentUrl
						+ ", new export url: " + newUrl);
			}
		}

		private List<URL> getMatchedUrls(List<URL> configuratorUrls, URL currentSubscribe) {
			List<URL> result = new ArrayList<URL>();
			for (URL url : configuratorUrls) {
				URL overrideUrl = url;
				// 兼容旧版本
				if (url.getParameter(Constants.CATEGORY_KEY) == null
						&& Constants.OVERRIDE_PROTOCOL.equals(url.getProtocol())) {
					overrideUrl = url.addParameter(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY);
				}

				// 检查是不是要应用到当前服务上
				if (UrlUtils.isMatch(currentSubscribe, overrideUrl)) {
					result.add(url);
				}
			}
			return result;
		}

		// 合并配置的url
		private URL getConfigedInvokerUrl(List<Configurator> configurators, URL url) {
			for (Configurator configurator : configurators) {
				url = configurator.configure(url);
			}
			return url;
		}
	}

	public static class InvokerDelegete<T> extends InvokerWrapper<T> {
		private final Invoker<T> invoker;

		/**
		 * @param invoker
		 * @param url
		 *            invoker.getUrl返回此值
		 */
		public InvokerDelegete(Invoker<T> invoker, URL url) {
			super(invoker, url);
			this.invoker = invoker;
		}

		public Invoker<T> getInvoker() {
			if (invoker instanceof InvokerDelegete) {
				return ((InvokerDelegete<T>) invoker).getInvoker();
			} else {
				return invoker;
			}
		}
	}

	/**
	 * exporter代理,建立返回的exporter与protocol
	 * export出的exporter的对应关系，在override时可以进行关系修改.
	 * 
	 * @author chao.liuc
	 *
	 * @param <T>
	 */
	private class ExporterChangeableWrapper<T> implements Exporter<T> {

		private Exporter<T> exporter;

		private final Invoker<T> originInvoker;

		public ExporterChangeableWrapper(Exporter<T> exporter, Invoker<T> originInvoker) {
			this.exporter = exporter;
			this.originInvoker = originInvoker;
		}

		public Invoker<T> getOriginInvoker() {
			return originInvoker;
		}

		public Invoker<T> getInvoker() {
			return exporter.getInvoker();
		}

		public void setExporter(Exporter<T> exporter) {
			this.exporter = exporter;
		}

		public void unexport() {
			String key = getCacheKey(this.originInvoker);
			bounds.remove(key);
			exporter.unexport();
		}
	}
}