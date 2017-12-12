package net.jahhan.extension.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.frameworkx.annotation.Activate;
import lombok.extern.slf4j.Slf4j;
import net.jahhan.cache.CustomCacheKeyCreater;
import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.annotation.Cache;
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

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Activate(group = Constants.PROVIDER, order = 1000)
@Extension("interfaceCacheFilter")
@Singleton
@Slf4j
public class InterfaceCacheFilter implements Filter {
	private Redis redis = RedisFactory.getRedis(RedisFactory.DEFAULT_DATABASE, null);
	private static String PRE = "fast_back:";
	private static String LOCK_PRE = "fast_back_lock:";

	private String createrCacheKey(Cache cache, String interfaceClassName, Method implMethod, Invocation invocation) {
		StringBuilder sb = new StringBuilder(PRE);
		sb.append("_").append(interfaceClassName).append("_").append(implMethod);
		if (cache.isCustomCacheKey()) {
			Class createrClass = cache.customCacheKeyCreaterClass();
			CustomCacheKeyCreater keyCreater = null;
			if (createrClass != null && CustomCacheKeyCreater.class.isAssignableFrom(createrClass)) {
				try {
					keyCreater = (CustomCacheKeyCreater) createrClass.newInstance();
				} catch (Exception e) {
					log.error("service customCacheKeyCreater error!!", e);
				}
			}

			if (keyCreater != null) {
				String key = keyCreater.createCacheKey(invocation.getAttachments(), invocation.getArguments());
				sb.append("_createCacheKey:").append(key);
				return sb.toString();
			}

			int[] indexArr = cache.argumentIndexNumbers();
			Object[] args = invocation.getArguments();
			if (indexArr != null && args != null && args.length > 0) {
				int maxIndex = args.length - 1;
				sb.append("_createCacheKey(");
				int len = sb.length();
				for (int idx : indexArr) {
					if (idx >= 0 && idx <= maxIndex) {
						sb.append("index[").append(idx).append("]:").append(args[idx]).append("_");
					}
				}
				if (sb.length() > len) {
					sb.deleteCharAt(sb.length() - 1);
				}
				sb.append(")");
				return sb.toString();
			}
		}

		BaseVariable baseVariable = BaseVariable.getBaseVariable();
		sb.append("_").append(baseVariable.getSign());

		return sb.toString();
	}

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
			switch (cache.fastBackType()) {

			case USERID:
				User user = AuthenticationVariable.getAuthenticationVariable().getUser();
				Assert.notNull(user, "无用户信息", JahhanErrorCode.NO_AUTHORITY);
				key = createrCacheKey(cache, interfaceClassName, implMethod, invocation) + "_"
						+ user.getUserId();
				break;
			case ALL:
				key = createrCacheKey(cache, interfaceClassName, implMethod, invocation);
				break;
			default:
				break;

			}
			byte[] bytes = redis.getBinary(key.getBytes());
			if (bytes != null) {
				if (cache.fastBackFail()) {
					throw new JahhanException(JahhanErrorCode.FAST_RESPONSE_ERROR, cache.fastBackFailMessage());
				}
				Result deserialize = SerializerUtil.deserialize(bytes, Result.class);
				log.debug("快速返回：" + interfaceClassName + "." + implMethod);
				return deserialize;
			}
			String ret = "";
			TimeUnit blockTimeUnit = cache.blockTimeUnit();
			try (DistributedLock lock = ServiceReentrantLockUtil.lock(LOCK_PRE + interfaceClassName + "." + implMethod,
					cache.blockTime(), blockTimeUnit)) {
				bytes = redis.getBinary(key.getBytes());
				if (bytes != null) {
					if (cache.fastBackFail()) { // fastBackFail=true 快速失败 ,
						// fastBackFail=false 快速返回
						throw new JahhanException(JahhanErrorCode.FAST_RESPONSE_ERROR, cache.fastBackFailMessage());
					}
					Result deserialize = SerializerUtil.deserialize(bytes, Result.class);
					log.debug("快速返回：" + interfaceClassName + "." + implMethod);
					return deserialize;
				}
				invoke = invoker.invoke(invocation);
				ret = redis.setNxTTL(key.getBytes(), SerializerUtil.serializeFrom(invoke), cache.blockTime(),
						blockTimeUnit);
			} catch (Exception e) {
				log.error("错误", e);
				throw new JahhanException(e);
			}
			if (cache.fastBackFail() && (null == ret || !ret.equals("OK"))) {
				throw new JahhanException(JahhanErrorCode.LOCK_ERROE, "快速返回失败错误");
			}
		}
		return invoke;
	}
}