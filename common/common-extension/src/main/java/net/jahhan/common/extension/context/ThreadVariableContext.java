package net.jahhan.common.extension.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程局部变量
 * 
 */
public class ThreadVariableContext {
	private Map<String, Variable> variableMap;

	public ThreadVariableContext() {
		reset();
	}

	public void putVariable(String type, Variable variable) {
		variableMap.put(type, variable);
	}

	public Variable getVariable(String type) {
		return variableMap.get(type);
	}

	public void reset() {
		variableMap = new HashMap<>();
	}
}
