package net.jahhan.extension.filter.interfacecache;

import java.lang.reflect.Method;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.frameworkx.annotation.Activate;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.annotation.Cache;
import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.util.SerializerUtil;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.context.BaseVariable;
import net.jahhan.exception.JahhanException;
import net.jahhan.lock.DistributedLock;
import net.jahhan.lock.util.ServiceReentrantLockUtil;
import net.jahhan.service.context.AuthenticationVariable;
import net.jahhan.service.service.bean.User;
import net.jahhan.spi.Filter;

@Activate(group = Constants.PROVIDER, order = 1000)
@Extension("interfaceCacheFilter")
@Singleton
@Slf4j
public class InterfaceCacheFilter implements Filter {
	private Redis redis = RedisFactory.getRedis(RedisFactory.DEFAULT_DATABASE, null);
	private static String PRE = "fast_back:";
	private static String LOCK_PRE = "fast_back_lock:";

	public Result invoke(Invoker<?> invoker, Invocation invocation) throws JahhanException {
		String interfaceClassName = invoker.getUrl().getParameter("interface");
		String implClassName = invoker.getUrl().getParameter("class");
		String methodName = invocation.getMethodName();

		Method implMethod = null;
		try {
			implMethod = Class.forName(implClassName).getDeclaredMethod(methodName,
					invocation.getParameterTypes());
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new JahhanException(JahhanErrorCode.UNKNOW_ERROR, "未知错误", e);
		}
		Cache cache = implMethod.getAnnotation(Cache.class);
		Result invoke;
		if (null == cache || cache.blockTime() < 1) {
			return invoker.invoke(invocation);
		} else {
			String key = "";
			BaseVariable baseVariable = BaseVariable.getBaseVariable();
			switch (cache.fastBackType()) {

			case USERID:
				User user = AuthenticationVariable.getAuthenticationVariable().getUser();
				Assert.notNull(user, "无用户信息", JahhanErrorCode.NO_AUTHORITY);
				key = PRE + interfaceClassName + "." + implMethod + "_" + baseVariable.getSign() + "_"
						+ user.getUserId();
				break;
			case ALL:
				key = PRE + interfaceClassName + "." + implMethod + "_" + baseVariable.getSign();
				break;
			default:
				break;

			}
			byte[] bytes = redis.getBinary(key.getBytes());
			if (bytes != null) {
				if (cache.fastBackFail()) {
					throw new JahhanException(JahhanErrorCode.FAST_RESPONSE_ERROR, "快速返回失败");
				}
				Result deserialize = SerializerUtil.deserialize(bytes, Result.class);
				log.debug("快速返回：" + interfaceClassName + "." + implMethod);
				return deserialize;
			}
			String ret = "";
			try (DistributedLock lock = ServiceReentrantLockUtil.lock(LOCK_PRE + interfaceClassName + "." + implMethod,
					cache.blockTime())) {
				bytes = redis.getBinary(key.getBytes());
				if (bytes != null) {
					if (cache.fastBackFail()) { // fastBackFail=true 快速失败 ,
						// fastBackFail=false 快速返回
						throw new JahhanException(JahhanErrorCode.FAST_RESPONSE_ERROR, "快速返回失败");
					}
					Result deserialize = SerializerUtil.deserialize(bytes, Result.class);
					log.debug("快速返回：" + interfaceClassName + "." + implMethod);
					return deserialize;
				}
				invoke = invoker.invoke(invocation);
				ret = redis.setNxTTL(key.getBytes(), SerializerUtil.serializeFrom(invoke), cache.blockTime());
			} catch (Exception e) {
				log.error("错误" ,e);
				throw new JahhanException(e);
			}
			if (cache.fastBackFail() && (null == ret || !ret.equals("OK"))) {
				throw new JahhanException(JahhanErrorCode.LOCK_ERROE, "快速返回失败错误");
			}
		}
		return invoke;
	}
}