package net.jahhan.spring.aspect;

import net.jahhan.common.extension.exception.JahhanException;

public interface Filter {
	Object invoke(Invoker invoker, Invocation invocation) throws JahhanException;
}
