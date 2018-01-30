package net.jahhan.spring.aspect;

import java.lang.reflect.Method;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;

public interface Invocation {

	/**
	 * get method .
	 *
	 * @return method .
	 * @serial
	 */
	Method getMethod();
	
	Class<?> getImplClass();

	/**
	 * get parameter types.
	 *
	 * @return parameter types.
	 * @serial
	 */
	Class<?>[] getParameterTypes();

	/**
	 * get arguments.
	 *
	 * @return arguments.
	 * @serial
	 */
	Object[] getArguments();

	/**
	 * get attachments.
	 *
	 * @return attachments.
	 * @serial
	 */
	Map<String, String> getAttachments();

	/**
	 * get attachment by key.
	 *
	 * @return attachment value.
	 * @serial
	 */
	String getAttachment(String key);

	/**
	 * get attachment by key with default value.
	 *
	 * @return attachment value.
	 * @serial
	 */
	String getAttachment(String key, String defaultValue);

	ProceedingJoinPoint getProceedingJoinPoint();

	/**
	 * get the invoker in current context.
	 *
	 * @return invoker.
	 * @transient
	 */
	Invoker getInvoker();

}
