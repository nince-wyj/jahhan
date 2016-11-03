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
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;

import net.jahhan.annotation.DbLogisticsConn;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.DBLogisticsConnectionType;
import net.jahhan.context.ApplicationContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.dblogistics.DBConnExecutorHandler;
import net.jahhan.dblogistics.DBConnExecutorHelper;
import net.jahhan.exception.BussinessException;
import net.jahhan.exception.FrameworkException;
import net.jahhan.exception.NoRollBackException;

@Activate(group = Constants.PROVIDER)
public class DBLogisticsFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(DBLogisticsFilter.class);

	public Result invoke(Invoker<?> invoker, Invocation inv) throws RpcException {
		ApplicationContext applicationContext = ApplicationContext.CTX;
		InvocationContext invocationContext = new InvocationContext();
		applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);

		String interfaceClassName = inv.getAttachment("interface");
		String methodName = inv.getMethodName();
		Result result = new RpcResult();

		Class<?> interfaceClass;
		Method method = null;
		try {
			interfaceClass = DBLogisticsFilter.class.getClassLoader().loadClass(interfaceClassName);
			method = interfaceClass.getDeclaredMethod(methodName, inv.getParameterTypes());
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e1) {
			throw new RpcException(SystemErrorCode.UNKOWN_ERROR, "未知错误", e1);
		}
		DbLogisticsConn dbLogisticsConn = method.getAnnotation(DbLogisticsConn.class);
		DBLogisticsConnectionType dBLogisticsConnectionType;
		if (null == dbLogisticsConn) {
			dBLogisticsConnectionType = DBLogisticsConnectionType.NONE;
		} else {
			dBLogisticsConnectionType = dbLogisticsConn.value();
		}
		if (dBLogisticsConnectionType.equals(DBLogisticsConnectionType.NONE)) {
			result = invoker.invoke(inv);
		} else {
			invocationContext.setDBLogisticsConnType(dBLogisticsConnectionType);
			DBConnExecutorHandler dbConnExecutorHandler = DBConnExecutorHelper
					.getDBConnExecutorHandler(dBLogisticsConnectionType);
			try {
				dbConnExecutorHandler.beginConnection();
				result = invoker.invoke(inv);
				Throwable throwable = result.getException();
				if (null != throwable && !(throwable instanceof NoRollBackException)) {
					logger.error("TransactionFilter SystemException {}", throwable);
					dbConnExecutorHandler.rollback();
				} else {
					dbConnExecutorHandler.commit();
				}
			} catch (BussinessException e) {
				dbConnExecutorHandler.rollback();
				throw new RpcException(e.getCode(), e.getMessage());
			} catch (FrameworkException e) {
				logger.error("TransactionFilter SystemException {}", e);
				dbConnExecutorHandler.rollback();
				throw new RpcException(e.getCode(), e.getMessage());
			} catch (Exception e) {
				logger.error(e);
				throw new RpcException(SystemErrorCode.UNKOWN_ERROR, "未知错误", e);
			} finally {
				dbConnExecutorHandler.close();
				invocationContext.setDBLogisticsConnType(DBLogisticsConnectionType.NONE);
			}
		}

		return result;
	}
}