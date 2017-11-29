package net.jahhan.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程局部变量
 * 
 */
public class VariableContext {
	private Map<String, Variable> variableMap;

	public VariableContext() {
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
		Variable baseVariable = new BaseVariable();
		variableMap.put("base", baseVariable);
	}
}
