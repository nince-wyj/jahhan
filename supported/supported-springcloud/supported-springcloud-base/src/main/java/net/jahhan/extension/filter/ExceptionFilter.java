package net.jahhan.extension.filter;

import javax.inject.Singleton;

import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.spring.aspect.Filter;
import net.jahhan.spring.aspect.Invocation;
import net.jahhan.spring.aspect.Invoker;

@Singleton
@Order(0)
public class ExceptionFilter implements Filter {

	public Object invoke(Invoker invoker, Invocation invocation) throws JahhanException {
		Object result = null;
		try {
			result = invoker.invoke(invocation);
			return result;
		} catch (JahhanException e) {
			throw e;
		} catch (Throwable e) {
			StringBuilder sb = new StringBuilder();
			sb.append("exception message:");
			LogUtil.requestError(sb.toString(), e);
			JahhanException.throwException(JahhanErrorCode.UNKNOW_ERROR, e.getMessage());
		}
		return result;
	}

}