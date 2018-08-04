package com.frameworkx.common.extension.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.frameworkx.annotation.Activate;
import com.frameworkx.annotation.Adaptive;
import com.frameworkx.common.extension.utils.extension.ActivateComparator;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import lombok.Getter;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.utils.ExtensionUtil;

public abstract class ExtensionExtendUtil extends ExtensionUtil{
	@Getter
	private static final Map<Class<?>, Map<String, Activate>> cachedActivates = new ConcurrentHashMap<>();

	public static <T> T getExtension(Class<T> type, String name) {
		Injector injector = BaseContext.CTX.getInjector();
		return injector.getInstance(Key.get(type, Names.named(name)));
	}

	public static <T> T getExtensionDirect(Class<T> type, String name) {
		Injector injector = BaseContext.CTX.getInjector();
		T instance = injector.getInstance(Key.get(type, Names.named("$" + name)));
		return instance;
	}

	public static <T> T getExtension(Class<T> type) {
		Injector injector = BaseContext.CTX.getInjector();
		return injector.getInstance(type);
	}

	public static <T> T getAdaptiveExtension(Class<T> type) {
		Injector injector = BaseContext.CTX.getInjector();
		try {
			return injector.getInstance(Key.get(type, Adaptive.class));
		} catch (Exception e) {
			throw new IllegalStateException("fail to create adaptive instance: " + e.toString(), e);
		}
	}

	public static <T> Set<String> getSupportedExtensions(Class<T> type) {
		Map<Class<?>, String> map = extensionCacheClassNameMap.get(type);
		if (null != map) {
			Collection<String> values = map.values();
			Set<String> re = new HashSet<>();
			for (String value : values) {
				re.add(value);
			}
			return re;
		}
		return null;
	}

	public static <T> String getExtensionName(Class<T> type, Class<? extends Object> extension) {
		Map<Class<?>, String> map = extensionCacheClassNameMap.get(type);
		if (null != map) {
			return map.get(extension);
		}
		return null;
	}

	public static <T> boolean hasExtension(Class<T> type, String extension) {
		Map<Class<?>, String> map = extensionCacheClassNameMap.get(type);
		if (null != map) {
			return map.containsValue(extension);
		}
		return false;
	}

	public static <T> List<T> getActivateExtension(Class<T> type, URL url, String key) {
		return getActivateExtension(type, url, key, null);
	}

	public static <T> List<T> getActivateExtension(Class<T> type, URL url, String[] values) {
		return getActivateExtension(type, url, values, null);
	}

	public static <T> List<T> getActivateExtension(Class<T> type, URL url, String key, String group) {
		String value = url.getParameter(key);
		return getActivateExtension(type, url,
				value == null || value.length() == 0 ? null : Constants.COMMA_SPLIT_PATTERN.split(value), group);
	}

	public static <T> List<T> getActivateExtension(Class<T> type, URL url, String[] values, String group) {
		List<T> exts = new ArrayList<T>();
		List<String> names = values == null ? new ArrayList<String>(0) : Arrays.asList(values);
		if (!names.contains(Constants.REMOVE_VALUE_PREFIX + Constants.DEFAULT_KEY)) {
			Map<String, Activate> map = cachedActivates.get(type);
			if (null != map) {
				for (Map.Entry<String, Activate> entry : map.entrySet()) {
					String name = entry.getKey();
					Activate activate = entry.getValue();
					if (isMatchGroup(group, activate.group())) {
						T ext = getExtension(type, name);
						if (!names.contains(name) && !names.contains(Constants.REMOVE_VALUE_PREFIX + name)
								&& isActive(activate, url)) {
							exts.add(ext);
						}
					}
				}
			}
			Collections.sort(exts, ActivateComparator.COMPARATOR);
		}
		List<T> usrs = new ArrayList<T>();
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			if (!name.startsWith(Constants.REMOVE_VALUE_PREFIX)
					&& !names.contains(Constants.REMOVE_VALUE_PREFIX + name)) {
				if (Constants.DEFAULT_KEY.equals(name)) {
					if (usrs.size() > 0) {
						exts.addAll(0, usrs);
						usrs.clear();
					}
				} else {
					T ext = getExtension(type, name);
					usrs.add(ext);
				}
			}
		}
		if (usrs.size() > 0) {
			exts.addAll(usrs);
		}
		return exts;
	}

	private static boolean isMatchGroup(String group, String[] groups) {
		if (group == null || group.length() == 0) {
			return true;
		}
		if (groups != null && groups.length > 0) {
			for (String g : groups) {
				if (group.equals(g)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isActive(Activate activate, URL url) {
		String[] keys = activate.value();
		if (keys == null || keys.length == 0) {
			return true;
		}
		for (String key : keys) {
			for (Map.Entry<String, String> entry : url.getParameters().entrySet()) {
				String k = entry.getKey();
				String v = entry.getValue();
				if ((k.equals(key) || k.endsWith("." + key)) && ConfigUtils.isNotEmpty(v)) {
					return true;
				}
			}
		}
		return false;
	}
}
