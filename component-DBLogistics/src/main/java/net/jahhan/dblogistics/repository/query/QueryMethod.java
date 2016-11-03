package net.jahhan.dblogistics.repository.query;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import net.jahhan.dblogistics.annotation.DocQuery;
import net.jahhan.dblogistics.annotation.NeoQuery;
import net.jahhan.dblogistics.repository.parameter.DefaultParameters;
import net.jahhan.dblogistics.repository.parameter.Parameters;

public class QueryMethod {

	private final Method method;
	private final NeoQuery neoAnnotation;
	private final DocQuery docAnnotation;
	private final Parameters<?, ?> parameters;

	public QueryMethod(Method method) {
		this.method = method;
		this.neoAnnotation = method.getAnnotation(NeoQuery.class);
		this.docAnnotation = method.getAnnotation(DocQuery.class);
		this.parameters = createParameters(method);
	}

	public NeoQuery getNeoQuery() {
		return neoAnnotation;
	}

	public DocQuery getDocQuery() {
		return docAnnotation;
	}

	public Method getMethod() {
		return method;
	}

	/**
	 * Creates a {@link Parameters} instance.
	 * 
	 * @param method
	 * @return must not return {@literal null}.
	 */
	protected Parameters<?, ?> createParameters(Method method) {
		return new DefaultParameters(method);
	}

	/**
	 * Returns the {@link Parameters} wrapper to gain additional information
	 * about {@link Method} parameters.
	 * 
	 * @return
	 */
	public Parameters<?, ?> getParameters() {
		return parameters;
	}

	/**
	 * @return The concrete, non-generic return type of this query method -
	 *         i.e., the type to which graph database query results should be
	 *         mapped
	 */
	public Class<?> resolveConcreteReturnType() {
		Class<?> type = this.method.getReturnType();
		Type genericType = this.method.getGenericReturnType();

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
