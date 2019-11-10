package net.jahhan.extension.filter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.xml.transform.Result;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.cache.CustomCacheKeyCreater;
import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.annotation.Cache;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.cache.util.SerializerUtil;
import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.lock.DistributedLock;
import net.jahhan.lock.util.ServiceReentrantLockUtil;
import net.jahhan.service.service.bean.User;
import net.jahhan.spring.aspect.Filter;
import net.jahhan.spring.aspect.Invocation;
import net.jahhan.spring.aspect.Invoker;
import net.jahhan.variable.AuthenticationVariable;
import net.jahhan.variable.BaseThreadVariable;

@Singleton
@Slf4j
@Order(1000)
public class InterfaceCacheFilter implements Filter {
	private Redis redis = RedisFactory.getRedis(RedisFactory.DEFAULT_DATABASE, null);
	private static String PRE = "fast_back:";
	private static String LOCK_PRE = "fast_back_lock:";

	private String createrCacheKey(Cache cache, String interfaceClassName, Method implMethod, Invocation invocation) {
		StringBuilder sb = new StringBuilder(PRE);
		sb.append(interfaceClassName).append("_").append(implMethod);
		if (cache.isCustomCacheKey()) {
			Class createrClass = cache.customCacheKeyCreaterClass();
			CustomCacheKeyCreater keyCreater = null;
			if (createrClass != null) {
				if (CustomCacheKeyCreater.class.isAssignableFrom(createrClass)) {
					try {
						keyCreater = (CustomCacheKeyCreater) createrClass.newInstance();
					} catch (Exception e) {
						JahhanException.throwException(JahhanErrorCode.INSTANTIATION_ERROR,
								"customCacheKeyCreater:" + createrClass.getName() + " can't new instance error!", e);
					}
				} else {// 配置有问题抛异常，测试阶段就能发现
					JahhanException.throwException(JahhanErrorCode.CONFIGURATION_ERROR,
							"service:" + interfaceClassName + " method:" + implMethod + " customCacheKeyCreaterClass:"
									+ createrClass.getName()
									+ " isn't a sub class of CustomCacheKeyCreater.class error!!");
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
				sb.append("_customCacheKey(");
				int len = sb.length();
				List<Integer> errorIndexList = new ArrayList<>();
				for (int idx : indexArr) {
					if (idx >= 0 && idx <= maxIndex) {
						sb.append("index[").append(idx).append("]:").append(args[idx]).append("_");
					} else {
						errorIndexList.add(idx);
					}
				}
				if (errorIndexList.size() > 0) {// 配置有问题抛异常，测试阶段就能发现
					JahhanException.throwException(JahhanErrorCode.CONFIGURATION_ERROR,
							"service:" + interfaceClassName + " method:" + implMethod + " argumentIndexNumbers:"
									+ errorIndexList + " is invalid index!!");
				}
				if (sb.length() > len) {
					sb.deleteCharAt(sb.length() - 1);
				}
				sb.append(")");

				return sb.toString();
			}
		}

		BaseThreadVariable baseVariable = (BaseThreadVariable) BaseThreadVariable.getThreadVariable("base");
		sb.append("_").append(baseVariable.getSign());

		return sb.toString();
	}

	public Object invoke(Invoker invoker, Invocation invocation) throws JahhanException {
		Object invoke = null;
		if (RedisConstants.isInUse()) {
			Method method = invocation.getMethod();
			Cache cache = method.getAnnotation(Cache.class);

			if (null == cache || cache.blockTime() < 1) {
				return invoker.invoke(invocation);
			} else {
				String key = "";
				switch (cache.fastBackType()) {

				case USERID:
					User user = ((AuthenticationVariable) AuthenticationVariable.getThreadVariable("authentication")).getUser();
					Assert.notNull(user, "无用户信息", JahhanErrorCode.NO_AUTHORITY);
					key = createrCacheKey(cache, invocation.getImplClass().getName(), method, invocation) + "_" + user.getUserId();
					break;
				case ALL:
					key = createrCacheKey(cache, invocation.getImplClass().getName(), method, invocation);
					break;
				default:
					break;

				}
				log.trace("##cache key:{}", key);
				byte[] bytes = redis.getBinary(key.getBytes());
				TimeUnit blockTimeUnit = cache.blockTimeUnit();
				if (bytes != null) {
					if (cache.fastBackFail()) {
						throw new JahhanException(JahhanErrorCode.FAST_RESPONSE_ERROR, cache.fastBackFailMessage());
					}
					Object deserialize = SerializerUtil.deserialize(bytes, Object.class);
					long tempttl = cache.blockTime();
					if (!blockTimeUnit.equals(TimeUnit.MILLISECONDS)) {
						tempttl = blockTimeUnit.toMillis(cache.blockTime());
					}
					redis.pexpire(key, tempttl);
					log.debug("快速返回：" + invocation.getImplClass().getName() + "." + method);
					return deserialize;
				}
				String ret = "";
				try (DistributedLock lock = ServiceReentrantLockUtil
						.lock(LOCK_PRE + invocation.getImplClass().getName() + "." + method, cache.blockTime(), blockTimeUnit)) {
					bytes = redis.getBinary(key.getBytes());
					if (bytes != null) {
						if (cache.fastBackFail()) { // fastBackFail=true 快速失败 ,
							// fastBackFail=false 快速返回
							throw new JahhanException(JahhanErrorCode.FAST_RESPONSE_ERROR, cache.fastBackFailMessage());
						}
						Object deserialize = SerializerUtil.deserialize(bytes, Result.class);
						log.debug("快速返回：" + invocation.getImplClass().getName() + "." + method);
						return deserialize;
					}
					invoke = invoker.invoke(invocation);
					ret = redis.setNxTTL(key.getBytes(), SerializerUtil.serializeFrom(invoke), cache.blockTime(),
							blockTimeUnit);
				} catch (Exception e) {
					log.error("错误", e);
					if (!(e instanceof JahhanException
							&& JahhanErrorCode.LOCK_OVERTIME == ((JahhanException) e).getCode())) {
						throw new JahhanException(e);
					}
				}
				if (cache.fastBackFail() && (null == ret || !ret.equals("OK"))) {
					// 缓存设置不成功，但是数据库查询都能查询到数据，不要抛异常导致业务功能异常,只记录异常情况
					log.error("保存至redis错误，key:{}  value:{}", key, invoke);
					// throw new JahhanException(JahhanErrorCode.LOCK_ERROE,
					// "快速返回失败错误");
				}
			}
		} else {
			invoke = invoker.invoke(invocation);
		}
		return invoke;
	}
}