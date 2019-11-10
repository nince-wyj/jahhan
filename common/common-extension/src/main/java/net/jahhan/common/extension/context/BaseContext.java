package net.jahhan.common.extension.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;

import lombok.Getter;

/**
 * @author nince
 */
@Singleton
public class BaseContext {

	public BaseContext() {
		if (null == CTX) {
			CTX = this;
		}
	}

	public static BaseContext CTX;
	@Inject
	private Injector injector;
	@Inject
	private ThreadLocalUtil<ThreadVariableContext> threadLocalUtil;
	@Getter
	private Node node = Node.getInstance();
	private static Map<String, Variable> variableMap = new ConcurrentHashMap<>();

	public void putVariable(String type, Variable variable) {
		variableMap.put(type, variable);
	}

	public Variable getVariable(String type) {
		Variable variable = variableMap.get(type);
		if (null == variable) {
			Map<String, Class<? extends Variable>> globalMap = Variable.getGlobalMap();
			Class<? extends Variable> class1 = globalMap.get(type);
			try {
				variable = class1.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return variable;
	}

	public Injector getInjector() {
		return injector;
	}

	public ThreadLocalUtil<ThreadVariableContext> getThreadLocalUtil() {
		return threadLocalUtil;
	}

	public ThreadVariableContext getVariableContext() {
		ThreadVariableContext value = threadLocalUtil.getValue();
		if (null == value) {
			threadLocalUtil.openThreadLocal(new ThreadVariableContext());
			value = threadLocalUtil.getValue();
		}
		return value;
	}
}
