package net.jahhan.common.extension.context;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import net.jahhan.common.extension.annotation.GlobalVariable;
import net.jahhan.common.extension.annotation.ThreadVariable;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;

public abstract class Variable {
	@Getter
	private String name;
	private static Map<String, Class<? extends Variable>> threadMap = new HashMap<>();
	@Getter
	private static Map<String, Class<? extends Variable>> globalMap = new HashMap<>();

	public Variable() {
		ThreadVariable threadVariable = this.getClass().getAnnotation(ThreadVariable.class);
		if (null != threadVariable) {
			this.name = threadVariable.value();
			threadMap.put(threadVariable.value(), this.getClass());
		}
		GlobalVariable globalVariable = this.getClass().getAnnotation(GlobalVariable.class);
		if (null != globalVariable) {
			this.name = globalVariable.value();
			globalMap.put(globalVariable.value(), this.getClass());
		}
	}

	public static Variable getThreadVariable(String name) {
		ThreadVariableContext variableContext = BaseContext.CTX.getVariableContext();
		if (null == variableContext) {
			return null;
		}
		Variable variable = variableContext.getVariable(name);
		if (null == variable) {
			try {
				variable = threadMap.get(name).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			variableContext.putVariable(name, variable);
		}
		return variable;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Variable> T getThreadVariable(Class<T> clazz) {
		ThreadVariableContext variableContext = BaseContext.CTX.getVariableContext();
		if (null == variableContext) {
			return null;
		}
		ThreadVariable threadVariable = clazz.getAnnotation(ThreadVariable.class);
		Assert.notNull(threadVariable, clazz + "不是线程变量！", JahhanErrorCode.CONFIGURATION_ERROR);
		String name = threadVariable.value();
		Variable variable = variableContext.getVariable(name);
		if (null == variable) {
			try {
				variable = threadMap.get(name).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			variableContext.putVariable(name, variable);
		}
		return (T) variable;
	}

	public Variable getGlobalVariable(String name) {
		Variable variable = BaseContext.CTX.getVariable(name);
		if (null == variable) {
			try {
				variable = globalMap.get(name).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			BaseContext.CTX.putVariable(name, variable);
		}
		return variable;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Variable> T getGlobalVariable(Class<T> clazz) {
		GlobalVariable globalVariable = clazz.getAnnotation(GlobalVariable.class);
		Assert.notNull(globalVariable, clazz + "不是全局变量！", JahhanErrorCode.CONFIGURATION_ERROR);
		String name = globalVariable.value();
		Variable variable = BaseContext.CTX.getVariable(name);
		if (null == variable) {
			try {
				variable = globalMap.get(name).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			BaseContext.CTX.putVariable(name, variable);
		}
		return (T)variable;
	}
}
