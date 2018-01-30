package net.jahhan.spring.aspect;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;

import net.jahhan.request.context.RequestVariable;

public class RpcInvocation implements Invocation, Serializable {
	private static final long serialVersionUID = -4355285085441097045L;

	private Method method;

	private Class<?>[] parameterTypes;

	private Object[] arguments;

	private Map<String, String> attachments;

	private transient Invoker invoker;

	private ProceedingJoinPoint pjp;

	private Class<?> implClass;
	
	public RpcInvocation() {
	}

	public RpcInvocation(Method method, Object[] arguments, ProceedingJoinPoint pjp) {
		this(method, method.getParameterTypes(), arguments, null, null, pjp);
	}

	public RpcInvocation(Method method, Class<?>[] parameterTypes, Object[] arguments, Map<String, String> attachments,
			Invoker invoker, ProceedingJoinPoint pjp) {
		this.method = method;
		this.implClass = method.getDeclaringClass();
		this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
		this.arguments = arguments == null ? new Object[0] : arguments;
		this.attachments = attachments == null ? RequestVariable.getVariable().getAttachments() : attachments;
		this.invoker = invoker;
		this.pjp = pjp;
	}

	@Override
	public Method getMethod() {
		return this.method;
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return this.parameterTypes;
	}

	@Override
	public Object[] getArguments() {
		return this.arguments;
	}

	@Override
	public Map<String, String> getAttachments() {
		return this.attachments;
	}

	@Override
	public String getAttachment(String key) {
		return attachments.get(key);
	}

	@Override
	public String getAttachment(String key, String defaultValue) {
		if (attachments == null) {
			return null;
		}
		return attachments.get(key);
	}

	@Override
	public Invoker getInvoker() {
		return invoker;
	}

	@Override
	public ProceedingJoinPoint getProceedingJoinPoint() {
		return pjp;
	}

	@Override
	public Class<?> getImplClass() {
		return implClass;
	}
}
