package net.jahhan.cache.context;

import java.util.HashMap;
import java.util.Map;

import net.jahhan.context.BaseContext;
import net.jahhan.context.Variable;

/**
 * 数据库线程局部变量
 * 
 */
public class RedisVariable extends Variable {
	// 分布式锁
	private Map<String, String> lockMap = new HashMap<>();

	public boolean setLock(String lockType, String lockRadom) {
		if (lockMap.containsKey(lockType)) {
			return false;
		}
		lockMap.put(lockType, lockRadom);
		return true;
	}

	public String getLock(String lockType) {
		return lockMap.get(lockType);
	}

	public static RedisVariable getDBVariable() {
		RedisVariable variable = (RedisVariable) BaseContext.CTX.getVariableContext().getVariable("redis");
		if (null == variable) {
			variable = new RedisVariable();
			BaseContext.CTX.getVariableContext().putVariable("redis", variable);
		}
		return variable;
	}
}
