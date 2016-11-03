package net.jahhan.dblogistics.repository.query;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.annotations.EntityFactory;
import org.neo4j.ogm.context.SingleUseEntityMapper;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.session.GraphCallback;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;

import net.jahhan.dblogistics.annotation.DocQuery;
import net.jahhan.dblogistics.annotation.NeoQuery;

/**
 * Specialisation of {@link RepositoryQueryProxy} that handles mapping to object
 * annotated with <code>&#064;QueryResult</code>.
 *
 */
public class RepositoryQueryWithResultProxy extends RepositoryQueryProxy {

	/**
	 * Constructs a new {@link RepositoryQueryWithResultProxy} based on the
	 * given arguments.
	 *
	 * @param queryMethod
	 *            The {@link QueryMethod} to which this repository query
	 *            corresponds
	 * @param session
	 *            The OGM {@link Session} used to execute the query
	 */
	public RepositoryQueryWithResultProxy(QueryMethod queryMethod, Session session, Class<?> clazz) {
		super(queryMethod, session, clazz);
	}

	@Override
	protected Object execute(Class<?> returnType, final Class<?> concreteReturnType, NeoQuery neoQuery,
			DocQuery docQuery, Map<String, Object> queryParams) {
		String cypherQueryString = "";
		if (null != neoQuery) {
			cypherQueryString = getTemplateValue(".neo.value");
		}
		Collection<Object> resultObjects = concreteReturnType.isInterface()
				? mapToProxy(concreteReturnType, cypherQueryString, queryParams)
				: mapToConcreteType(concreteReturnType, cypherQueryString, queryParams);

		if (Iterable.class.isAssignableFrom(returnType)) {
			return resultObjects;
		}
		return resultObjects.isEmpty() ? null : resultObjects.iterator().next();
	}

	@SuppressWarnings("deprecation")
	private Collection<Object> mapToConcreteType(final Class<?> targetType, String cypherQuery,
			Map<String, Object> queryParams) {
		return this.session.doInTransaction(new GraphCallback<Collection<Object>>() {
			@Override
			public Collection<Object> apply(Request requestHandler, Transaction transaction, MetaData metaData) {
				Collection<Object> toReturn = new ArrayList<>();
				SingleUseEntityMapper entityMapper = new SingleUseEntityMapper(metaData, new EntityFactory(metaData));
				Iterable<Map<String, Object>> results = session.query(cypherQuery, queryParams);
				for (Map<String, Object> result : results) {
					toReturn.add(entityMapper.map(targetType, result));
				}
				return toReturn;
			}
		});
	}

	private Collection<Object> mapToProxy(Class<?> targetType, String cypherQuery, Map<String, Object> queryParams) {
		Iterable<Map<String, Object>> queryResults = this.session.query(cypherQuery, queryParams);

		Collection<Object> resultObjects = new ArrayList<>();
		Class<?>[] interfaces = new Class<?>[] { targetType };
		for (Map<String, Object> map : queryResults) {
			resultObjects.add(newProxyInstance(targetType.getClassLoader(), interfaces, new QueryResultProxy(map)));
		}
		return resultObjects;
	}

}
