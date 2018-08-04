package net.jahhan.spring.aspect;

import net.jahhan.common.extension.exception.JahhanException;

public interface Invoker {

	/**
	 * invoke.
	 *
	 * @param invocation
	 * @return result
	 * @throws RpcException
	 */
	Object invoke(Invocation invocation) throws JahhanException;

}
