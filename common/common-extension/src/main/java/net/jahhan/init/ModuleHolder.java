package net.jahhan.init;

import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class ModuleHolder {
	private Map<Integer, Class<?>> moduleMap;
	private Set<Class<?>> lazyModuleSet;
}