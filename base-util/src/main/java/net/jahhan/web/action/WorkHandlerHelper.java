package net.jahhan.web.action;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;

import net.jahhan.utils.ClassScaner;
import net.jahhan.web.action.annotation.ActionService;
import net.jahhan.web.action.annotation.HandlerAnnocation;

/**
 * 接口拦截器帮助类
 */
@Singleton
public class WorkHandlerHelper {

	private static Map<Integer, Class<? extends ActionHandler>> handlerMap = new TreeMap<>();

	@Inject
	private Injector injector;

	@SuppressWarnings("unchecked")
	public static void init() {
		List<String> classNameList = new ClassScaner().parse(new String[] { "net.jahhan.web.action.actionhandler" });
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try {
			for (String className : classNameList) {
				Class<? extends ActionHandler> clazz = (Class<? extends ActionHandler>) classLoader
						.loadClass(className);
				if (clazz.isAnnotationPresent(HandlerAnnocation.class) && ActionHandler.class.isAssignableFrom(clazz)) {
					HandlerAnnocation handlerAnnocation = clazz.getAnnotation(HandlerAnnocation.class);
					handlerMap.put(handlerAnnocation.value(), clazz);
				}
			}
		} catch (Exception e) {

		}
	}

	public ActionHandler registerActionChain(ActionHandler actionHandler, Class<?> clazz) throws Exception {
		Iterator<Integer> keyIt = handlerMap.keySet().iterator();
		ActionHandler nextHandler = actionHandler;
		while (keyIt.hasNext()) {
			Class<? extends ActionHandler> nextHandlerClass = handlerMap.get(keyIt.next());
			Class<?>[] argsClass = new Class[] { ActionHandler.class, ActionService.class };
			Constructor<? extends ActionHandler> cons = nextHandlerClass.getConstructor(argsClass);
			nextHandler = cons.newInstance(new Object[] { nextHandler, clazz.getAnnotation(ActionService.class) });
			injector.injectMembers(nextHandler);
		}
		return nextHandler;
	}
}
