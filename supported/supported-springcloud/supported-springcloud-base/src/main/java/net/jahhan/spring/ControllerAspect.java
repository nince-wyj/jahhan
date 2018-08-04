package net.jahhan.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.guice.injector.InjectorHolder;
import org.springframework.stereotype.Component;

import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.annotation.Order;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.spring.aspect.Filter;
import net.jahhan.spring.aspect.Invocation;
import net.jahhan.spring.aspect.Invoker;
import net.jahhan.spring.aspect.RpcInvocation;

@Aspect
@Component
@Slf4j
public class ControllerAspect {
	private static Invoker last;

	static {
		String[] packages = PackageUtil.packages("extension.filter");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		List<String> classNameList = new ClassScaner().parse(packages);
		Map<Integer, Filter> filterMap = new TreeMap<>();
		Injector injector = InjectorHolder.getInstance().getInjector();
		for (String className : classNameList) {
			try {
				Class<?> scanClass = classLoader.loadClass(className);
				Order order = scanClass.getAnnotation(Order.class);
				if (null != order && Filter.class.isAssignableFrom(scanClass)) {
					filterMap.put(order.value(), (Filter) injector.getInstance(scanClass));
				}
			} catch (ClassNotFoundException e) {
				log.error("",e);
			}
		}
		Collection<Filter> filters = filterMap.values();
		List<Filter> filterList = new ArrayList<>();
		filterList.addAll(filters);
		last = new Invoker() {
			@Override
			public Object invoke(Invocation invocation) throws JahhanException {
				try {
					return invocation.getProceedingJoinPoint().proceed();
				} catch (Throwable e) {
					log.error("",e);
					JahhanException.throwException(JahhanErrorCode.UNKNOW_ERROR, e.getMessage());
				}
				return null;
			}

		};
		if (filters.size() > 0) {
			for (int i = filters.size() - 1; i >= 0; i--) {
				final Filter filter = filterList.get(i);
				final Invoker next = last;
				last = new Invoker() {
					public Object invoke(Invocation invocation) throws JahhanException {
						return filter.invoke(next, invocation);
					}
				};
			}
		}
	}

	@Pointcut("execution(* *..controller..*Controller.*(..))")
	public void aspect() {
	}

	@Around("aspect()")
	public Object interceptor(ProceedingJoinPoint pjp) {
		MethodSignature signature = (MethodSignature) pjp.getSignature();
		Method method = signature.getMethod(); // 获取被拦截的方法
		Object[] args = pjp.getArgs();
		return last.invoke(new RpcInvocation(method, args, pjp));
	}
}
