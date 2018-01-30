package net.jahhan.spring.aspect;

import net.jahhan.exception.JahhanException;

public interface Filter {
	Object invoke(Invoker invoker, Invocation invocation) throws JahhanException;
}
