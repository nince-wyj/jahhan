package net.jahhan.dblogistics.repository.query;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.Utils;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.dblogistics.DblogisticContext;
import net.jahhan.dblogistics.annotation.DocQuery;
import net.jahhan.dblogistics.annotation.NeoQuery;
import net.jahhan.dblogistics.constant.DBLogisticsConf;
import net.jahhan.dblogistics.doc.CodecRegistryHolder;
import net.jahhan.dblogistics.doc.DocConnExecutorHandler;
import net.jahhan.dblogistics.entity.SuperEntity;
import net.jahhan.dblogistics.repository.RepositoryProviderHelper;
import net.jahhan.dblogistics.repository.RepositoryQuery;
import net.jahhan.dblogistics.repository.parameter.Parameter;
import net.jahhan.dblogistics.repository.parameter.Parameters;
import net.jahhan.dblogistics.utils.SessionUtils;
import net.jahhan.exception.FrameworkException;
import net.jahhan.utils.BeanTools;
import net.jahhan.utils.DynamicStringParser;
import net.jahhan.utils.JsonUtil;

public class RepositoryQueryProxy implements RepositoryQuery {

	private final QueryMethod queryMethod;
	private final Class<?> clazz;
	protected final Session session;

	public RepositoryQueryProxy(QueryMethod queryMethod, Session session, Class<?> clazz) {
		this.queryMethod = queryMethod;
		this.session = session;
		this.clazz = clazz;
	}

	private String methodName;
	private Map<String, Object> params;

	private Map<String, Object> getParams() {
		return params;
	}

	private void setParams(Map<String, Object> params) {
		this.params = params;
	}

	private String getMethodName() {
		return methodName;
	}

	private void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	private String getTemplatePre() {
		return clazz.getSimpleName() + "." + getMethodName();
	}

	protected String getTemplateValue(String templateKey) {
		Configuration freeMakerConf = DBLogisticsConf.getFreeMakerConf();

		StringWriter writer = new StringWriter();
		try {
			Template template = freeMakerConf.getTemplate(getTemplatePre() + templateKey, "utf-8");
			template.process(getParams(), writer);
			return writer.toString();
		} catch (Exception e) {
			FrameworkException.throwException(SystemErrorCode.CONTENT_FORMAT_ERROR, "获取模板值错误！", e);
		}
		FrameworkException.throwException(SystemErrorCode.CONTENT_FORMAT_ERROR, "获取模板值错误！");
		return "";
	}

	@Override
	public final Object execute(Object[] parameters) {
		Class<?> returnType = queryMethod.getMethod().getReturnType();
		setMethodName(queryMethod.getMethod().getName());
		Class<?> concreteType = queryMethod.resolveConcreteReturnType();
		Map<String, Object> params = resolveParams(parameters);
		setParams(params);
		NeoQuery neoQuery = getNeoQuery();
		return execute(returnType, concreteType, neoQuery, getDocQuery(), params);
	}

	protected Object execute(Class<?> returnType, Class<?> concreteType, NeoQuery neoQuery, DocQuery docQuery,
			Map<String, Object> queryParams) {
		String docQueryString = "";
		String projection = "";
		String sort = "";
		String skip = "";
		String limit = "";
		String cypherQueryString = "";
		if (null != neoQuery) {
			cypherQueryString = getTemplateValue(".neo.value");
		}
		if (DBLogisticsConf.isUseDoc() && null != docQuery) {
			docQueryString = docQuery.value();
			projection = docQuery.projection();
			sort = docQuery.sort();
			skip = docQuery.skip();
			limit = docQuery.limit();
		}
		if (cypherQueryString.equals("") && docQueryString.equals("")
				&& (null == docQuery || docQuery.aggregate().length == 0)) {
			FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "没有相关搜索语句！");
		}
		// 执行sql语句，不返回
		if (returnType.equals(Void.class) || returnType.equals(void.class)) {
			return exeSql(docQuery, cypherQueryString, queryParams);
		}
		// 返回集合
		if (Iterable.class.isAssignableFrom(returnType) && !queryReturnsStatistics()) {
			if (Map.class.isAssignableFrom(concreteType)) {
				// 返回map集合，暂时不返回doc数据
				return session.query(cypherQueryString, queryParams).queryResults();
			}
			return queryColletion(concreteType, docQueryString, neoQuery, docQuery, cypherQueryString, queryParams,
					projection, sort, skip, limit);
		}
		// 返回result，可用于搜路径，不查doc
		if (queryReturnsStatistics()) {
			return session.query(cypherQueryString, queryParams);
		}
		// 返回对象，若doc搜索不为空，先根据doc搜索出值，再查cypher
		if (DBLogisticsConf.isUseDoc() && SuperEntity.class.isAssignableFrom(returnType)
				&& (!docQueryString.equals("") || null != docQuery.aggregate() && docQuery.aggregate().length > 0)) {
			return queryObject(returnType, concreteType, neoQuery, docQuery, cypherQueryString, queryParams, projection,
					sort, skip, limit);
		}
		// 没有doc查询语句，查询cypher,返回单条
		Object query = session.queryForObject(returnType, cypherQueryString, queryParams);
		if (null == query) {
			return null;
		}
		Long ooid = ((SuperEntity) query).getOoid();
		if (DBLogisticsConf.isUseDoc() && neoQuery.useDoc()) {
			InvocationContext invocationContext = BaseContext.CTX.getInvocationContext();
			Object localCache = invocationContext.getLocalCachePojo(returnType, String.valueOf(ooid));
			if (null != localCache) {
				BeanTools.copyBean(localCache, query);
				DblogisticContext.instance().publishReadPojo(localCache, String.valueOf(ooid));
				return localCache;
			}
			if (!invocationContext.isDeletePojo(returnType, String.valueOf(ooid))) {
				Object cache = RepositoryProviderHelper.getInstance().get(returnType).get(String.valueOf(ooid),
						returnType);
				if (cache != null) {
					BeanTools.copyBean(cache, query);
					DblogisticContext.instance().publishReadPojo(cache, String.valueOf(ooid));
					return cache;
				}
			}
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			MongoCollection<?> collection = docReadExecutor.getMongoDatabase()
					.getCollection(concreteType.getName(), concreteType)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			String json = DynamicStringParser.parse("{'ooid' : '#ooid#'}", Utils.map("ooid", ooid));
			BasicDBObject filter = BasicDBObject.parse(json);
			FindIterable<?> find = collection.find(filter);
			if (!projection.equals("")) {
				BasicDBObject projectionObject = BasicDBObject.parse(getTemplateValue(".doc.projection"));
				find = find.projection(projectionObject);
			}
			if (!sort.equals("")) {
				BasicDBObject sortObject = BasicDBObject.parse(getTemplateValue(".doc.sort"));
				find = find.sort(sortObject);
			}
			if (!skip.equals("")) {
				find = find.skip(Integer.valueOf(getTemplateValue(".doc.skip")));
			}
			Object result = find.first();
			BeanTools.copyBean(result, query);
			DblogisticContext.instance().publishReadPojo(result, String.valueOf(ooid));
			return result;
		}
		DblogisticContext.instance().publishReadPojo(query, String.valueOf(ooid));
		return query;
	}

	private Map<String, Object> resolveParams(Object[] parameters) {

		Map<String, Object> params = new HashMap<>();
		Parameters<?, ?> methodParameters = queryMethod.getParameters();

		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = methodParameters.getParameter(i);
			// The parameter might be an entity, try to resolve its id
			Object parameterValue = session.resolveGraphIdFor(parameters[i]);
			if (parameterValue == null) { // Either not an entity or not
											// persisted
				parameterValue = parameters[i];
			}
			if (parameter.isSubDoc()) {
				parameterValue = JsonUtil.toJson(parameterValue);
			}
			if (parameter.isNamedParameter()) {
				params.put(parameter.getName(), parameterValue);
			} else {
				params.put("" + i, parameterValue);

			}
		}
		return params;
	}

	public QueryMethod getQueryMethod() {
		return queryMethod;
	}

	protected NeoQuery getNeoQuery() {
		return getQueryMethod().getNeoQuery();
	}

	protected DocQuery getDocQuery() {
		return getQueryMethod().getDocQuery();
	}

	private boolean queryReturnsStatistics() {
		Class<?> returnType = queryMethod.getMethod().getReturnType();
		return (returnType == QueryStatistics.class || returnType == Result.class);
	}

	/**
	 * 执行sql语句，不返回
	 * 
	 * @param docQuery
	 * @param cypherQueryString
	 * @param queryParams
	 * @return
	 */
	private Object exeSql(DocQuery docQuery, String cypherQueryString, Map<String, ?> queryParams) {
		if (DBLogisticsConf.isUseDoc() && null != docQuery) {
			BasicDBObject query = BasicDBObject.parse(getTemplateValue(".doc.value"));
			BasicDBObject projectionObject = BasicDBObject.parse(getTemplateValue(".doc.projection"));
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			MongoCollection<?> collection = docWriteExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			collection.updateOne(query, projectionObject);
		} else {
			session.query(cypherQueryString, queryParams);
		}
		return null;
	}

	/**
	 * 返回集合
	 * 
	 * @param concreteType
	 * @param docQueryString
	 * @param neoQuery
	 * @param docQuery
	 * @param cypherQueryString
	 * @param queryParams
	 * @param projection
	 * @param sort
	 * @param skip
	 * @param limit
	 * @return
	 */
	private Object queryColletion(Class<?> concreteType, String docQueryString, NeoQuery neoQuery, DocQuery docQuery,
			String cypherQueryString, Map<String, Object> queryParams, String projection, String sort, String skip,
			String limit) {
		// 返回对象集合，若doc搜索不为空，先搜索doc条件，再根据cypher条件搜索符合条件的对象
		if (DBLogisticsConf.isUseDoc() && SuperEntity.class.isAssignableFrom(concreteType)
				&& (!docQueryString.equals("") || null != docQuery.aggregate() && docQuery.aggregate().length > 0)) {
			String[] aggregates = docQuery.aggregate();
			if (aggregates.length > 0) {
				DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
				MongoCollection<?> collection = docReadExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
						.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
				List<BasicDBObject> pipeline = new ArrayList<>();
				for (int i = 0; i < aggregates.length; i++) {
					BasicDBObject query = BasicDBObject.parse(getTemplateValue(".doc.aggregate." + i));
					pipeline.add(query);
				}
				AggregateIterable<?> find = collection.aggregate(pipeline, clazz);
				List<Long> ooids = new ArrayList<>();
				Map<Long, Object> docResultMap = new HashMap<>();
				find.forEach(new Block<Object>() {
					@Override
					public void apply(Object o) {
						SuperEntity s = (SuperEntity) o;
						ooids.add(s.getOoid());
						docResultMap.put(s.getOoid(), o);
					}
				});
				if (!cypherQueryString.equals("") && docQuery.useNeo()) {
					Map<String, Object> param = new HashMap<>();
					param.put("ooids", ooids);
					String pre = DynamicStringParser.parse("MATCH (n:`%s`)-[*0..1]-() WHERE n.ooid in #ooids# WITH n",
							param);
					String cypher = pre + cypherQueryString;
					Iterable<?> neoResult = session.query(concreteType, cypher, queryParams);
					List<Object> result = new ArrayList<>();
					if (neoResult != null) {
						for (Object o : neoResult) {
							SuperEntity s = (SuperEntity) o;
							Object object = docResultMap.get(s.getOoid());
							BeanTools.copyBean(o, object);
							DblogisticContext.instance().publishReadPojo(o, String.valueOf(s.getOoid()));
							result.add(o);
						}
						return result;
					}
				}
				// cypher条件空，则先取缓存里的对象，再去cypher里搜未命中的数据回来填充
				List<Object> result = new ArrayList<>();
				List<Long> cachedOoids = new ArrayList<>();
				InvocationContext invocationContext = BaseContext.CTX.getInvocationContext();
				for (Long ooid : ooids) {
					Object localCache = invocationContext.getLocalCachePojo(concreteType, String.valueOf(ooid));
					if (null != localCache) {
						BeanTools.copyBean(localCache, docResultMap.get(ooid));
						result.add(localCache);
						DblogisticContext.instance().publishReadPojo(localCache, String.valueOf(ooid));
						cachedOoids.add(ooid);
						continue;
					}
				}
				ooids.removeAll(cachedOoids);
				if (ooids.isEmpty()) {
					return result;
				}
				cachedOoids.clear();
				// 搜redis缓存
				for (Long ooid : ooids) {
					if (!invocationContext.isDeletePojo(concreteType, String.valueOf(ooid))) {
						Object cache = RepositoryProviderHelper.getInstance().get(concreteType)
								.get(String.valueOf(ooid), concreteType);
						if (cache != null) {
							BeanTools.copyBean(cache, docResultMap.get(ooid));
							result.add(cache);
							DblogisticContext.instance().publishReadPojo(cache, String.valueOf(ooid));
							cachedOoids.add(ooid);
							continue;
						}
					}
				}
				ooids.removeAll(cachedOoids);
				if (ooids.isEmpty()) {
					return result;
				}
				if (docQuery.useNeo()) {
					String cypher = String.format("MATCH (n:`%s`)-[*0..1]-() WHERE n.ooid in { ooids } RETURN n",
							concreteType.getSimpleName());
					Iterable<?> loadAll = session.query(concreteType, cypher, Utils.map("ooids", ooids));
					Map<Long, Object> neoResultMap = new HashMap<>();
					for (Object o : loadAll) {
						SuperEntity s = (SuperEntity) o;
						neoResultMap.put(s.getOoid(), o);
					}
					for (Long ooid : ooids) {
						Object object = neoResultMap.get(ooid);
						Object o = docResultMap.get(ooid);
						BeanTools.copyBean(o, object);
						result.add(o);
						DblogisticContext.instance().publishReadPojo(o, String.valueOf(ooid));
					}
				}
				return result;
			} else {
				BasicDBObject query = BasicDBObject.parse(getTemplateValue(".doc.value"));
				DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
				MongoCollection<?> collection = docReadExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
						.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
				FindIterable<?> find = collection.find(query);
				if (!projection.equals("")) {
					BasicDBObject projectionObject = BasicDBObject.parse(getTemplateValue(".doc.projection"));
					find = find.projection(projectionObject);
				}
				if (!sort.equals("")) {
					BasicDBObject sortObject = BasicDBObject.parse(getTemplateValue(".doc.sort"));
					find = find.sort(sortObject);
				}
				if (!skip.equals("")) {
					find = find.skip(Integer.valueOf(getTemplateValue(".doc.skip")));
				}
				if (!limit.equals("")) {
					find = find.limit(Integer.valueOf(getTemplateValue(".doc.limit")));
				}

				List<Long> ooids = new ArrayList<>();
				Map<Long, Object> docResultMap = new HashMap<>();
				find.forEach(new Block<Object>() {
					@Override
					public void apply(Object o) {
						SuperEntity s = (SuperEntity) o;
						ooids.add(s.getOoid());
						docResultMap.put(s.getOoid(), o);
					}
				});
				if (!cypherQueryString.equals("") && docQuery.useNeo()) {
					Map<String, Object> param = new HashMap<>();
					param.put("ooids", ooids);
					String pre = DynamicStringParser.parse("MATCH (n:`%s`)-[*0..1]-() WHERE n.ooid in #ooids# WITH n",
							param);
					String cypher = pre + cypherQueryString;
					Iterable<?> neoResult = session.query(concreteType, cypher, queryParams);
					List<Object> result = new ArrayList<>();
					if (neoResult != null) {
						for (Object o : neoResult) {
							SuperEntity s = (SuperEntity) o;
							Object object = docResultMap.get(s.getOoid());
							BeanTools.copyBean(o, object);
							DblogisticContext.instance().publishReadPojo(o, String.valueOf(s.getOoid()));
							result.add(o);
						}
						return result;
					}
				}
				// cypher条件空，则先取缓存里的对象，再去cypher里搜未命中的数据回来填充
				List<Object> result = new ArrayList<>();
				List<Long> cachedOoids = new ArrayList<>();
				InvocationContext invocationContext = BaseContext.CTX.getInvocationContext();
				for (Long ooid : ooids) {
					Object localCache = invocationContext.getLocalCachePojo(concreteType, String.valueOf(ooid));
					if (null != localCache) {
						BeanTools.copyBean(localCache, docResultMap.get(ooid));
						result.add(localCache);
						DblogisticContext.instance().publishReadPojo(localCache, String.valueOf(ooid));
						cachedOoids.add(ooid);
						continue;
					}
				}
				ooids.removeAll(cachedOoids);
				if (ooids.isEmpty()) {
					return result;
				}
				cachedOoids.clear();
				// 搜redis缓存
				for (Long ooid : ooids) {
					if (!invocationContext.isDeletePojo(concreteType, String.valueOf(ooid))) {
						Object cache = RepositoryProviderHelper.getInstance().get(concreteType)
								.get(String.valueOf(ooid), concreteType);
						if (cache != null) {
							BeanTools.copyBean(cache, docResultMap.get(ooid));
							result.add(cache);
							DblogisticContext.instance().publishReadPojo(cache, String.valueOf(ooid));
							cachedOoids.add(ooid);
							continue;
						}
					}
				}
				ooids.removeAll(cachedOoids);
				if (ooids.isEmpty()) {
					return result;
				}
				if (docQuery.useNeo()) {
					String cypher = String.format("MATCH (n:`%s`)-[*0..1]-() WHERE n.ooid in { ooids } RETURN n",
							concreteType.getSimpleName());
					Iterable<?> loadAll = session.query(concreteType, cypher, Utils.map("ooids", ooids));
					Map<Long, Object> neoResultMap = new HashMap<>();
					for (Object o : loadAll) {
						SuperEntity s = (SuperEntity) o;
						neoResultMap.put(s.getOoid(), o);
					}
					for (Long ooid : ooids) {
						Object object = neoResultMap.get(ooid);
						Object o = docResultMap.get(ooid);
						BeanTools.copyBean(o, object);
						result.add(o);
						DblogisticContext.instance().publishReadPojo(o, String.valueOf(ooid));
					}
				}
				return result;
			}

		}
		// 返回对象集合,doc搜索条件为空
		Iterable<?> neoResult = session.query(concreteType, cypherQueryString, queryParams);
		if (DBLogisticsConf.isUseDoc() && neoQuery.useDoc()) {
			Map<Long, Object> neoResultMap = new HashMap<>();
			List<Long> ooids = new ArrayList<>();
			for (Object o : neoResult) {
				SuperEntity s = (SuperEntity) o;
				ooids.add(s.getOoid());
				neoResultMap.put(s.getOoid(), o);
			}
			// 先搜本地缓存
			List<Long> cachedOoids = new ArrayList<>();
			List<Object> result = new ArrayList<>();
			InvocationContext invocationContext = BaseContext.CTX.getInvocationContext();
			for (Long ooid : ooids) {
				Object localCache = invocationContext.getLocalCachePojo(concreteType, String.valueOf(ooid));
				if (null != localCache) {
					BeanTools.copyBean(localCache, neoResultMap.get(ooid));
					result.add(localCache);
					DblogisticContext.instance().publishReadPojo(localCache, String.valueOf(ooid));
					cachedOoids.add(ooid);
					continue;
				}
			}
			ooids.removeAll(cachedOoids);
			if (ooids.isEmpty()) {
				return result;
			}
			cachedOoids.clear();
			// 搜redis缓存
			for (Long ooid : ooids) {
				if (!invocationContext.isDeletePojo(concreteType, String.valueOf(ooid))) {
					Object cache = RepositoryProviderHelper.getInstance().get(concreteType).get(String.valueOf(ooid),
							concreteType);
					if (cache != null) {
						BeanTools.copyBean(cache, neoResultMap.get(ooid));
						result.add(cache);
						DblogisticContext.instance().publishReadPojo(cache, String.valueOf(ooid));
						cachedOoids.add(ooid);
						continue;
					}
				}
			}
			ooids.removeAll(cachedOoids);
			if (ooids.isEmpty()) {
				return result;
			}
			// 搜doc数据库
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			MongoCollection<?> collection = docReadExecutor.getMongoDatabase()
					.getCollection(concreteType.getName(), concreteType)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			String json = DynamicStringParser.parse("{'ooid' : {'$in' : #ooids#}}", Utils.map("ooids", ooids));
			BasicDBObject filter = BasicDBObject.parse(json);
			FindIterable<?> find = collection.find(filter);
			if (!projection.equals("")) {
				BasicDBObject projectionObject = BasicDBObject.parse(getTemplateValue(".doc.projection"));
				find = find.projection(projectionObject);
			}
			if (!sort.equals("")) {
				BasicDBObject sortObject = BasicDBObject.parse(getTemplateValue(".doc.sort"));
				find = find.sort(sortObject);
			}
			if (!skip.equals("")) {
				find = find.skip(Integer.valueOf(getTemplateValue(".doc.skip")));
			}
			if (!limit.equals("")) {
				find = find.limit(Integer.valueOf(getTemplateValue(".doc.limit")));
			}
			find.forEach(new Block<Object>() {
				@Override
				public void apply(Object o) {
					Long ooid = ((SuperEntity) o).getOoid();
					BeanTools.copyBean(o, neoResultMap.get(ooid));
					result.add(o);
					DblogisticContext.instance().publishReadPojo(o, String.valueOf(ooid));
				}

			});
			return result;
		}
		return neoResult;
	}

	/**
	 * 返回对象，若doc搜索不为空，先根据doc搜索出值，再查cypher
	 * @param returnType
	 * @param concreteType
	 * @param neoQuery
	 * @param docQuery
	 * @param cypherQueryString
	 * @param queryParams
	 * @param projection
	 * @param sort
	 * @param skip
	 * @param limit
	 * @return
	 */
	private Object queryObject(Class<?> returnType, Class<?> concreteType, NeoQuery neoQuery, DocQuery docQuery,
			String cypherQueryString, Map<String, Object> queryParams, String projection, String sort, String skip,
			String limit) {
		String[] aggregates = docQuery.aggregate();
		if (aggregates.length > 0) {
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			MongoCollection<?> collection = docReadExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			List<BasicDBObject> pipeline = new ArrayList<>();
			for (int i = 0; i < aggregates.length; i++) {
				BasicDBObject query = BasicDBObject.parse(getTemplateValue(".doc.aggregate." + i));
				pipeline.add(query);
			}
			AggregateIterable<?> find = collection.aggregate(pipeline, clazz);
			if (!docQuery.useNeo()) {
				return find.first();
			}
			if (!cypherQueryString.equals("")) {
				List<Long> ooids = new ArrayList<>();
				Map<Long, Object> docResultMap = new HashMap<>();
				find.forEach(new Block<Object>() {
					@Override
					public void apply(Object o) {
						SuperEntity s = (SuperEntity) o;
						ooids.add(s.getOoid());
						docResultMap.put(s.getOoid(), o);
					}

				});
				Map<String, Object> param = new HashMap<>();
				param.put("ooids", ooids);
				String pre = DynamicStringParser.parse("MATCH (n:`%s`)-[*0..1]-() WHERE n.ooid in #ooids# WITH n",
						param);
				String cypher = pre + cypherQueryString;
				Object neoResult = session.queryForObject(returnType, cypher, queryParams);
				if (neoResult != null) {
					SuperEntity s = (SuperEntity) neoResult;
					Object object = docResultMap.get(s.getOoid());
					BeanTools.copyBean(neoResult, object);
					DblogisticContext.instance().publishReadPojo(neoResult, String.valueOf(s.getOoid()));
					return neoResult;
				}
			}
			Object first = find.first();
			Long ooid = ((SuperEntity) first).getOoid();
			String cypher = String.format("MATCH (n:`%s`)-[*0..1]-() WHERE n.ooid = { ooid } RETURN n",
					concreteType.getSimpleName());
			Object load = session.queryForObject(concreteType, cypher, Utils.map("ooid", ooid));
			BeanTools.copyBean(load, first);
			DblogisticContext.instance().publishReadPojo(load, String.valueOf(ooid));
			return load;
		} else {
			BasicDBObject query = BasicDBObject.parse(getTemplateValue(".doc.value"));
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocReadExecutor();
			MongoCollection<?> collection = docWriteExecutor.getMongoDatabase()
					.getCollection(returnType.getName(), returnType)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			FindIterable<?> find = collection.find(query);
			if (!projection.equals("")) {
				BasicDBObject projectionObject = BasicDBObject.parse(getTemplateValue(".doc.projection"));
				find = find.projection(projectionObject);
			}
			if (!sort.equals("")) {
				BasicDBObject sortObject = BasicDBObject.parse(getTemplateValue(".doc.sort"));
				find = find.sort(sortObject);
			}
			if (!skip.equals("")) {
				find = find.skip(Integer.valueOf(getTemplateValue(".doc.skip")));
			}
			if (!limit.equals("")) {
				find = find.limit(Integer.valueOf(getTemplateValue(".doc.limit")));
			}
			if (!docQuery.useNeo()) {
				return find.first();
			}
			if (!cypherQueryString.equals("")) {
				List<Long> ooids = new ArrayList<>();
				Map<Long, Object> docResultMap = new HashMap<>();
				find.forEach(new Block<Object>() {
					@Override
					public void apply(Object o) {
						SuperEntity s = (SuperEntity) o;
						ooids.add(s.getOoid());
						docResultMap.put(s.getOoid(), o);
					}

				});
				Map<String, Object> param = new HashMap<>();
				param.put("ooids", ooids);
				String pre = DynamicStringParser.parse("MATCH (n:`%s`)-[*0..1]-() WHERE n.ooid in #ooids# WITH n",
						param);
				String cypher = pre + cypherQueryString;
				Object neoResult = session.queryForObject(returnType, cypher, queryParams);
				if (neoResult != null) {
					SuperEntity s = (SuperEntity) neoResult;
					Object object = docResultMap.get(s.getOoid());
					BeanTools.copyBean(neoResult, object);
					DblogisticContext.instance().publishReadPojo(neoResult, String.valueOf(s.getOoid()));
					return neoResult;
				}
			}
			Object first = find.first();
			Long ooid = ((SuperEntity) first).getOoid();
			String cypher = String.format("MATCH (n:`%s`)-[*0..1]-() WHERE n.ooid = { ooid } RETURN n",
					concreteType.getSimpleName());
			Object load = session.queryForObject(concreteType, cypher, Utils.map("ooid", ooid));
			BeanTools.copyBean(load, first);
			DblogisticContext.instance().publishReadPojo(load, String.valueOf(ooid));
			return load;
		}
	}
}