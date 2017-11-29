package net.jahhan.common.extension.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import lombok.Getter;
import net.jahhan.context.BaseContext;

public abstract class ExtensionUtil {
	@Getter
	private volatile static Map<Class<?>, Map<Class<?>, String>> extensionCacheClassNameMap = new ConcurrentHashMap<>();

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
}
