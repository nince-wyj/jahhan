package net.jahhan.dblogistics.repository.query;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.dblogistics.SuperRepository;
import net.jahhan.dblogistics.annotation.DocQuery;
import net.jahhan.dblogistics.annotation.NeoQuery;
import net.jahhan.dblogistics.annotation.QueryResult;
import net.jahhan.dblogistics.entity.SuperEntity;
import net.jahhan.dblogistics.repository.RepositoryProviderHelper;
import net.jahhan.dblogistics.utils.SessionUtils;
import net.jahhan.exception.FrameworkException;

public class QueryProxy implements InvocationHandler {

	private Class<? extends SuperEntity> clazz;

	public QueryProxy(Class<? extends SuperEntity> clazz) {
		this.clazz = clazz;
	}

	protected static final Logger logger = LoggerFactory.getLogger(QueryProxy.class);

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (method.getAnnotation(NeoQuery.class) != null || method.getAnnotation(DocQuery.class) != null) {
				QueryMethod queryMethod = new QueryMethod(method);

				Session session = SessionUtils.getNeoReadSession();
				if (resolveConcreteReturnType(method).isAnnotationPresent(QueryResult.class)) {
					return new RepositoryQueryWithResultProxy(queryMethod, session, clazz).execute(args);
				}
				return new RepositoryQueryProxy(queryMethod, session, clazz).execute(args);
			}
			SuperRepository<?> foo = RepositoryProviderHelper.getInstance().get(clazz);
			return method.invoke(foo, args);
		} catch (Exception e) {
			FrameworkException.throwException(SystemErrorCode.UNKOWN_ERROR, "未知错误！", e);
		}
		return null;
	}

	public Class<?> resolveConcreteReturnType(Method method) {
		Class<?> type = method.getReturnType();
		Type genericType = method.getGenericReturnType();

		if (Iterable.class.isAssignableFrom(type)) {
			if (genericType instanceof ParameterizedType) {
				ParameterizedType returnType = (ParameterizedType) genericType;
				Type componentType = returnType.getActualTypeArguments()[0];

				return componentType instanceof ParameterizedType
						? (Class<?>) ((ParameterizedType) componentType).getRawType() : (Class<?>) componentType;
			} else {
				return Object.class;
			}
		}
		return type;
	}
}