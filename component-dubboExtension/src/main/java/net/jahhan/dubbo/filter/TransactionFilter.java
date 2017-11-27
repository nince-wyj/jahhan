package net.jahhan.dubbo.filter;

import java.lang.reflect.Method;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.frameworkx.exception.FrameWorkXException;
import com.alibaba.dubbo.rpc.RpcResult;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.dbconnexecutor.DBConnExecutorFactory;
import net.jahhan.dubbo.annotation.DBConnect;
import net.jahhan.exception.BussinessException;
import net.jahhan.exception.FrameworkException;
import net.jahhan.exception.NoRollBackException;

@Activate(group = Constants.PROVIDER)
public class TransactionFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(TransactionFilter.class);

	public Result invoke(Invoker<?> invoker, Invocation inv) throws FrameWorkXException {
		BaseContext applicationContext = BaseContext.CTX;
		InvocationContext invocationContext = new InvocationContext();
		applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);

		String interfaceClassName = inv.getAttachment("interface");
		String methodName = inv.getMethodName();
		DBConnectionType dBConnectionType = DBConnectionType.NONE;
		Result result = new RpcResult();

		Class<?> interfaceClass;
		Method method = null;
		try {
			interfaceClass = TransactionFilter.class.getClassLoader().loadClass(interfaceClassName);
			method = interfaceClass.getDeclaredMethod(methodName, inv.getParameterTypes());
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e1) {
			throw new FrameWorkXException(SystemErrorCode.UNKOWN_ERROR, "未知错误", e1);
		}
		DBConnect dBConnect = method.getAnnotation(DBConnect.class);
		if (null != dBConnect) {
			dBConnectionType = dBConnect.value();
		}
		invocationContext.setConnectionType(dBConnectionType);
		DBConnExecutorFactory connExec = new DBConnExecutorFactory(dBConnectionType);
		try {

			connExec.beginConnection();
			result = invoker.invoke(inv);
			Throwable throwable = result.getException();
			if (null != throwable && !(throwable instanceof NoRollBackException)) {
				logger.error("TransactionFilter SystemException {}", throwable);
				connExec.rollback();
			} else {
				connExec.endConnection();
			}
		} catch (BussinessException e) {
			connExec.rollback();
			throw new FrameWorkXException(e.getCode(), e.getMessage());
		} catch (FrameworkException e) {
			logger.error("TransactionFilter SystemException {}", e);
			connExec.rollback();
			throw new FrameWorkXException(e.getCode(), e.getMessage());
		} catch (Exception e) {
			logger.error(e);
			throw new FrameWorkXException(SystemErrorCode.UNKOWN_ERROR, "未知错误", e);
		} finally {
			connExec.close();
			invocationContext.setConnectionType(DBConnectionType.NONE);
		}
		return result;
	}
}