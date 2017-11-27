package net.jahhan.dblogistics.repository.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BsonDocumentWrapper;
import org.bson.Document;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.Utils;
import org.slf4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import net.jahhan.cache.RedisConstants;
import net.jahhan.cache.repository.AbstractSmpTTLCountRepository;
import net.jahhan.cache.util.SeqRepository;
import net.jahhan.cache.util.TagUtil;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.event.EventOperate;
import net.jahhan.db.publish.EventPublisherManager;
import net.jahhan.dblogistics.DblogisticContext;
import net.jahhan.dblogistics.SuperRepository;
import net.jahhan.dblogistics.constant.DBLogisticsConf;
import net.jahhan.dblogistics.doc.CodecRegistryHolder;
import net.jahhan.dblogistics.doc.DocConnExecutorHandler;
import net.jahhan.dblogistics.domain.Page;
import net.jahhan.dblogistics.domain.PageImpl;
import net.jahhan.dblogistics.domain.Pageable;
import net.jahhan.dblogistics.domain.Sort;
import net.jahhan.dblogistics.entity.SuperEntity;
import net.jahhan.dblogistics.utils.SessionUtils;
import net.jahhan.demand.DBEventListener;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.utils.Assert;
import net.jahhan.utils.BeanTools;
import net.jahhan.utils.DynamicStringParser;
import net.jahhan.utils.IterableUtils;

@SuppressWarnings("unchecked")
public class SuperRepositoryImpl<T extends SuperEntity> extends AbstractSmpTTLCountRepository
		implements SuperRepository<T>, DBEventListener {
	protected final Logger logger = LoggerFactory.getInstance().getLogger(SuperRepositoryImpl.class);
	private final Class<T> clazz;

	public SuperRepositoryImpl(Class<T> clazz) {
		this.clazz = clazz;
		EventPublisherManager.addListener(this);
	}

	private static final int DEFAULT_QUERY_DEPTH = 1;

	@Override
	public T save(T t) {
		Assert.isNull(t.getGraphId(), "节点已存在！", SystemErrorCode.CODE_ERROR);
		Session neo4jSession = SessionUtils.getNeoWriteSession();
		long inc = SeqRepository.inc("seq:entity_" + clazz.getSimpleName());
		t.setOoid(inc);
		neo4jSession.save(t, 0);
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			MongoCollection<T> collection = docWriteExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			collection.insertOne(t);
		}
		DblogisticContext.instance().publishDataModifyEvent(t, EventOperate.INSERT, String.valueOf(t.getOoid()));
		return t;
	}

	@Override
	public Iterable<T> save(Iterable<T> entities) {
		Session neo4jSession = SessionUtils.getNeoWriteSession();
		for (T entity : entities) {
			Assert.isNull(entity.getGraphId(), "节点已存在！", SystemErrorCode.CODE_ERROR);
			long inc = SeqRepository.inc("seq:entity_" + clazz.getSimpleName());
			entity.setOoid(inc);
			neo4jSession.save(entity, 0);
			if (DBLogisticsConf.isUseDoc()) {
				DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
				MongoCollection<T> collection = docWriteExecutor.getMongoDatabase()
						.getCollection(clazz.getName(), clazz)
						.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
				collection.insertOne(entity);
			}
			DblogisticContext.instance().publishDataModifyEvent(entity, EventOperate.INSERT,
					String.valueOf(entity.getOoid()));
		}
		return entities;
	}

	@Override
	public void delete(Long ooid) {
		T o = findOne(ooid);
		if (o != null) {
			Session session = SessionUtils.getNeoWriteSession();
			session.delete(o);
			if (DBLogisticsConf.isUseDoc()) {
				DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
				MongoCollection<T> collection = docWriteExecutor.getMongoDatabase()
						.getCollection(clazz.getName(), clazz)
						.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
				BasicDBObject filter = new BasicDBObject();
				filter.append("ooid", ooid);
				collection.deleteOne(filter);
			}
			DblogisticContext.instance().publishDeleteEvent(o, String.valueOf(ooid));
		}
	}

	@Override
	public void delete(T entity) {
		Session session = SessionUtils.getNeoWriteSession();
		session.delete(entity);
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			MongoCollection<T> collection = docWriteExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			BasicDBObject filter = new BasicDBObject();
			filter.append("ooid", entity.getOoid());
			collection.deleteOne(filter);
		}
		DblogisticContext.instance().publishDeleteEvent(entity, String.valueOf(entity.getOoid()));
	}

	@Override
	public void delete(Iterable<T> entities) {
		Session session = SessionUtils.getNeoWriteSession();
		for (T t : entities) {
			session.delete(t);
			if (DBLogisticsConf.isUseDoc()) {
				DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
				MongoCollection<T> collection = docWriteExecutor.getMongoDatabase()
						.getCollection(clazz.getName(), clazz)
						.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
				BasicDBObject filter = new BasicDBObject();
				filter.append("ooid", t.getOoid());
				collection.deleteOne(filter);
			}
			DblogisticContext.instance().publishDeleteEvent(t, String.valueOf(t.getOoid()));
		}
	}

	@Override
	public void deleteAll() {
		Session session = SessionUtils.getNeoWriteSession();
		session.deleteAll(clazz);
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			MongoCollection<T> collection = docWriteExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			collection.drop();
		}
	}

	@Override
	public void update(T entity) {
		Assert.notNull(entity.getGraphId(), "节点不存在！", SystemErrorCode.CODE_ERROR);
		Assert.notNull(entity.getOoid(), "节点不存在！", SystemErrorCode.CODE_ERROR);
		Session session = SessionUtils.getNeoWriteSession();
		session.save(entity);
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			MongoCollection<T> collection = docWriteExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			BasicDBObject filter = new BasicDBObject();
			filter.append("ooid", entity.getOoid());
			collection.replaceOne(filter, entity);
		}
		DblogisticContext.instance().publishDataModifyEvent(entity, EventOperate.UPDATE,
				String.valueOf(entity.getOoid()));
	}

	@Override
	public void updateNeo(T entity) {
		Assert.notNull(entity.getGraphId(), "节点不存在！", SystemErrorCode.CODE_ERROR);
		Assert.notNull(entity.getOoid(), "节点不存在！", SystemErrorCode.CODE_ERROR);
		Session session = SessionUtils.getNeoWriteSession();
		session.save(entity);
		DblogisticContext.instance().publishDataModifyEvent(entity, EventOperate.PART_MODIFY,
				String.valueOf(entity.getOoid()));
	}

	@Override
	public void updateDoc(Long ooid, T entity) {
		if (DBLogisticsConf.isUseDoc()) {
			Assert.notNull(ooid, "节点不存在！", SystemErrorCode.CODE_ERROR);
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			MongoCollection<T> collection = docWriteExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			BasicDBObject filter = new BasicDBObject();
			filter.append("ooid", ooid);
			collection.updateOne(filter,
					new Document("$set", BsonDocumentWrapper.asBsonDocument(entity, collection.getCodecRegistry())));
			DblogisticContext.instance().publishDataModifyEvent(entity, EventOperate.PART_MODIFY,
					String.valueOf(entity.getOoid()));
		}
	}

	@Override
	public void resetDoc(Long ooid, T entity) {
		if (DBLogisticsConf.isUseDoc()) {
			Assert.notNull(ooid, "节点不存在！", SystemErrorCode.CODE_ERROR);
			DocConnExecutorHandler docWriteExecutor = SessionUtils.getDocWriteExecutor();
			MongoCollection<T> collection = docWriteExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			BasicDBObject filter = new BasicDBObject();
			filter.append("ooid", ooid);
			collection.replaceOne(filter, entity);
			DblogisticContext.instance().publishDataModifyEvent(entity, EventOperate.PART_MODIFY,
					String.valueOf(entity.getOoid()));
		}
	}

	@Override
	public T findOne(Long ooid) {
		return findOne(ooid, DEFAULT_QUERY_DEPTH);
	}

	@Override
	public T findOne(Long ooid, int depth) {
		Session neo4jSession = SessionUtils.getNeoReadSession();
		T load = IterableUtils.getSingleOrNull(neo4jSession.loadAll(clazz, new Filter("ooid", ooid), depth));
		if (DBLogisticsConf.isUseDoc()) {
			InvocationContext invocationContext = BaseContext.CTX.getInvocationContext();
			T localCache = (T) invocationContext.getLocalCachePojo(clazz, String.valueOf(ooid));
			if (null != localCache) {
				BeanTools.copyBean(localCache, load);
				return localCache;
			}
			if (!invocationContext.isDeletePojo(clazz, String.valueOf(load.getOoid()))) {
				T cache = get(String.valueOf(load.getOoid()), clazz);
				if (cache != null) {
					BeanTools.copyBean(cache, load);
					return cache;
				}
			}
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			MongoCollection<T> collection = docReadExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			BasicDBObject filter = new BasicDBObject();
			filter.append("ooid", ooid);
			FindIterable<T> find = collection.find(filter);
			T first = find.first();
			if (load != null) {
				BeanTools.copyBean(first, load);
				DblogisticContext.instance().publishReadPojo(load, String.valueOf(load.getOoid()));
				return first;
			}
		}
		return load;
	}

	@Override
	public Iterable<T> findByOoids(Iterable<Long> ooids) {
		return findByOoids(ooids, null, DEFAULT_QUERY_DEPTH);
	}

	@Override
	public Iterable<T> findByOoids(Iterable<Long> ooids, int depth) {
		return findByOoids(ooids, null, depth);
	}

	@Override
	public Iterable<T> findByOoids(Iterable<Long> ooids, Sort sort) {
		return findByOoids(ooids, sort, DEFAULT_QUERY_DEPTH);
	}

	@Override
	public Iterable<T> findByOoids(Iterable<Long> ooids, Sort sort, int depth) {
		Session session = SessionUtils.getNeoReadSession();
		String cypher = String.format("MATCH (n:`%s`)-[*0..1]-() WHERE n.ooid in { ooids } RETURN n",
				clazz.getSimpleName());
		Iterable<T> loadAll = session.query(clazz, cypher, Utils.map("ooids", ooids));
		Map<Long, T> neoResultMap = new HashMap<>();
		List<Long> ooidList = new ArrayList<>();
		for (T t : loadAll) {
			neoResultMap.put(t.getOoid(), t);
			ooidList.add(t.getOoid());
		}
		if (DBLogisticsConf.isUseDoc()) {
			InvocationContext invocationContext = BaseContext.CTX.getInvocationContext();
			List<T> result = new ArrayList<>();
			List<Long> cachedOoids = new ArrayList<>();
			for (Long ooid : ooidList) {
				T localCache = (T) invocationContext.getLocalCachePojo(clazz, String.valueOf(ooid));
				if (null != localCache) {
					BeanTools.copyBean(localCache, neoResultMap.get(ooid));
					result.add(localCache);
					DblogisticContext.instance().publishReadPojo(localCache, String.valueOf(ooid));
					cachedOoids.add(ooid);
					continue;
				}
			}
			ooidList.removeAll(cachedOoids);
			if (ooidList.isEmpty()) {
				return result;
			}
			cachedOoids.clear();
			// 搜redis缓存
			for (Long ooid : ooidList) {
				if (!invocationContext.isDeletePojo(clazz, String.valueOf(ooid))) {
					T cache = get(String.valueOf(ooid), clazz);
					if (cache != null) {
						BeanTools.copyBean(cache, neoResultMap.get(ooid));
						result.add(cache);
						DblogisticContext.instance().publishReadPojo(cache, String.valueOf(ooid));
						cachedOoids.add(ooid);
						continue;
					}
				}
			}
			ooidList.removeAll(cachedOoids);
			if (ooidList.isEmpty()) {
				return result;
			}
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			MongoCollection<T> collection = docReadExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			String json = DynamicStringParser.parse("{'ooid' : {'$in' : #ooids#}}", Utils.map("ooids", ooidList));
			BasicDBObject filter = BasicDBObject.parse(json);
			FindIterable<T> docResult = collection.find(filter);
			docResult.forEach(new Block<T>() {
				@Override
				public void apply(T o) {
					Long ooid = o.getOoid();
					BeanTools.copyBean(o, neoResultMap.get(ooid));
					result.add(o);
					DblogisticContext.instance().publishReadPojo(o, String.valueOf(ooid));
				}
			});
			return result;
		}
		return loadAll;
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		return findAll(pageable, DEFAULT_QUERY_DEPTH);
	}

	@Override
	public Page<T> findAll(Pageable pageable, int depth) {
		Session session = SessionUtils.getNeoReadSession();
		Collection<T> data = session.loadAll(clazz, convert(pageable.getSort()),
				new Pagination(pageable.getPageNumber(), pageable.getPageSize()), depth);
		Map<Long, T> neoResultMap = new HashMap<>();
		List<Long> ooidList = new ArrayList<>();
		for (T t : data) {
			neoResultMap.put(t.getOoid(), t);
			ooidList.add(t.getOoid());
		}
		if (DBLogisticsConf.isUseDoc()) {
			InvocationContext invocationContext = BaseContext.CTX.getInvocationContext();
			List<T> result = new ArrayList<>();
			List<Long> cachedOoids = new ArrayList<>();
			for (Long ooid : ooidList) {
				T localCache = (T) invocationContext.getLocalCachePojo(clazz, String.valueOf(ooid));
				if (null != localCache) {
					BeanTools.copyBean(localCache, neoResultMap.get(ooid));
					result.add(localCache);
					DblogisticContext.instance().publishReadPojo(localCache, String.valueOf(ooid));
					cachedOoids.add(ooid);
					continue;
				}
			}
			ooidList.removeAll(cachedOoids);
			if (ooidList.isEmpty()) {
				return updatePage(pageable, new ArrayList<T>(result));
			}
			cachedOoids.clear();
			// 搜redis缓存
			for (Long ooid : ooidList) {
				if (!invocationContext.isDeletePojo(clazz, String.valueOf(ooid))) {
					T cache = get(String.valueOf(ooid), clazz);
					if (cache != null) {
						BeanTools.copyBean(cache, neoResultMap.get(ooid));
						result.add(cache);
						DblogisticContext.instance().publishReadPojo(cache, String.valueOf(ooid));
						cachedOoids.add(ooid);
						continue;
					}
				}
			}
			ooidList.removeAll(cachedOoids);
			if (ooidList.isEmpty()) {
				return updatePage(pageable, new ArrayList<T>(result));
			}
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			MongoCollection<T> collection = docReadExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			String json = DynamicStringParser.parse("{'ooid' : {'$in' : #ooids#}}", Utils.map("ooids", ooidList));
			BasicDBObject filter = BasicDBObject.parse(json);
			FindIterable<T> docResult = collection.find(filter);
			docResult.forEach(new Block<T>() {
				@Override
				public void apply(T o) {
					Long ooid = o.getOoid();
					BeanTools.copyBean(o, neoResultMap.get(ooid));
					result.add(o);
					DblogisticContext.instance().publishReadPojo(o, String.valueOf(ooid));
				}
			});
			return updatePage(pageable, new ArrayList<T>(result));
		}
		return updatePage(pageable, new ArrayList<T>(data));
	}

	@Override
	public boolean exists(Long ooid) {
		return findOne(ooid) != null;
	}

	@Override
	public long count() {
		if (DBLogisticsConf.isUseDoc()) {
			DocConnExecutorHandler docReadExecutor = SessionUtils.getDocReadExecutor();
			MongoCollection<T> collection = docReadExecutor.getMongoDatabase().getCollection(clazz.getName(), clazz)
					.withCodecRegistry(CodecRegistryHolder.getInstance().getCodecRegistry());
			return collection.count();
		} else {
			Session session = SessionUtils.getNeoReadSession();
			return session.countEntitiesOfType(clazz);
		}
	}

	private SortOrder convert(Sort sort) {
		SortOrder sortOrder = new SortOrder();
		if (sort != null) {
			for (Sort.Order order : sort) {
				if (order.isAscending()) {
					sortOrder.add(order.getProperty());
				} else {
					sortOrder.add(SortOrder.Direction.DESC, order.getProperty());
				}
			}
		}
		return sortOrder;
	}

	private Page<T> updatePage(Pageable pageable, List<T> results) {
		int pageSize = pageable.getPageSize();
		int pageOffset = pageable.getOffset();
		int total = pageOffset + results.size() + (results.size() == pageSize ? pageSize : 0);

		return new PageImpl<T>(results, pageable, total);
	}

	private final static String PRE = "EntityRep:";

	@Override
	protected String getKey(String id) {
		return PRE + clazz.getSimpleName() + "_" + id;
	}

	@Override
	protected String getType() {
		return RedisConstants.TABLE_SYSTEM;
	}

	@Override
	public void listen(EventObject event) {
		onListen(event, clazz);
	}

	@Override
	public String[] getTags() {
		return TagUtil.getTags(clazz);
	}

}
