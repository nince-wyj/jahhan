package net.jahhan.demo.dao.abstrimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.SqlSession;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.cache.repository.common.SeqRepository;
import net.jahhan.cache.util.SerializerUtil;
import net.jahhan.common.extension.utils.ListUtils;
import net.jahhan.demo.dao.TUserDemoInfoDao;
import net.jahhan.demo.dao.listen.TUserDemoInfoRep;
import net.jahhan.demo.pojo.TUserDemoInfo;
import net.jahhan.demo.pojo.page.TUserDemoInfoPage;
import net.jahhan.jdbc.context.DBContext;
import net.jahhan.jdbc.dopage.PagedResult;
import net.jahhan.jdbc.event.EventOperate;
import net.jahhan.jdbc.utils.ValidationUtil;
import net.jahhan.jdbc.validategroup.Create;
import net.jahhan.jdbc.validategroup.Modify;
import net.jahhan.variable.DBVariable;

/*
 * 自动生成,开发人员请勿修改.
 * 
 * @author code-generate-service
 */
@Slf4j(topic = "dao.TUserDemoInfoDao")
public abstract class AbstrTUserDemoInfoImpl implements TUserDemoInfoDao {

	protected String dataSource = "demo";
	@Inject
	private TUserDemoInfoRep repository;
	@Inject
	protected DBContext dbContext;

	/**
	 * 是否使用缓存
	 * 
	 * @return true表示使用缓存，false不使用
	 */
	protected abstract boolean isCachable();

	/**
	 * 缓存主键
	 */
	protected String pkToString(TUserDemoInfo tUserDemoInfo) {
		return ((Number) tUserDemoInfo.getUserId()).toString();
	}

	/** 增加 **/
	public int addTUserDemoInfo(TUserDemoInfo tUserDemoInfo) {
		tUserDemoInfo.setUserId(SeqRepository.inc("seq_t_user_demo_info"));
		tUserDemoInfo.setModifyTimestamp(System.currentTimeMillis());
		ValidationUtil.validate(tUserDemoInfo, Create.class);
		SqlSession session = dbContext.getWriteSession(dataSource);
		int ret = session.insert("TUSERDEMOINFO.addTUserDemoInfo", tUserDemoInfo);
		if (ret > 0) {
			dbContext.publishDataModifyEvent(dataSource, session.getConnection(), tUserDemoInfo, EventOperate.INSERT,
					pkToString(tUserDemoInfo));
		}
		return ret;
	}

	/** 批量增加 **/
	public int addBatchTUserDemoInfo(List<TUserDemoInfo> tUserDemoInfoList) {
		SqlSession session = dbContext.getBatchSession(dataSource);
		int i = 0;
		for (TUserDemoInfo tUserDemoInfo : tUserDemoInfoList) {
			tUserDemoInfo.setUserId(SeqRepository.inc("seq_t_user_demo_info"));
			tUserDemoInfo.setModifyTimestamp(System.currentTimeMillis());
			addBatchTUserDemoInfo(session, tUserDemoInfo);
			i++;
		}
		session.flushStatements();
		return i;
	}

	private void addBatchTUserDemoInfo(SqlSession session, TUserDemoInfo tUserDemoInfo) {
		ValidationUtil.validate(tUserDemoInfo, Create.class);
		int ret = session.insert("TUSERDEMOINFO.addTUserDemoInfo", tUserDemoInfo);
		if (ret > 0) {
			dbContext.publishDataModifyEvent(dataSource, session.getConnection(), tUserDemoInfo, EventOperate.INSERT,
					pkToString(tUserDemoInfo));
		}
	}

	/** 删除 **/
	public int delTUserDemoInfo(long userId) {
		TUserDemoInfo tUserDemoInfo = new TUserDemoInfo();
		tUserDemoInfo.setUserId(userId);
		SqlSession session = dbContext.getWriteSession(dataSource);
		int ret = session.delete("TUSERDEMOINFO.delTUserDemoInfo", tUserDemoInfo);
		if (ret > 0) {
			dbContext.publishDeleteEvent(dataSource, session.getConnection(), tUserDemoInfo, pkToString(tUserDemoInfo));
		}
		return ret;

	}

	/** 根据主键列表批量删除 **/
	public int delByIds(List<Long> userIds) {
		if (null != userIds && userIds.size() > 0) {
			SqlSession session = dbContext.getWriteSession(dataSource);
			int ret = session.delete("TUSERDEMOINFO.delTUserDemoInfoByIds", userIds);
			if (ret > 0) {
				for (Long userId : userIds) {
					TUserDemoInfo tUserDemoInfo = new TUserDemoInfo();
					tUserDemoInfo.setUserId(userId);
					dbContext.publishDeleteEvent(dataSource, session.getConnection(), tUserDemoInfo,
							pkToString(tUserDemoInfo));
				}
			}
			return ret;
		}
		log.debug("userIds is empty!");
		return 0;
	}

	/**
	 * 根据对象主键更新对象所有字段 注意：属性为Null也会更新置空
	 */
	public int resetAllTUserDemoInfo(long userId, TUserDemoInfo tUserDemoInfo) {
		tUserDemoInfo.setUserId(userId);
		tUserDemoInfo.setModifyTimestamp(System.currentTimeMillis());
		SqlSession session = dbContext.getWriteSession(dataSource);
		int ret = session.update("TUSERDEMOINFO.updateTUserDemoInfo", tUserDemoInfo);
		if (ret > 0) {
			dbContext.publishDataModifyEvent(dataSource, session.getConnection(), tUserDemoInfo, EventOperate.UPDATE,
					pkToString(tUserDemoInfo));
		}
		return ret;
	}

	/**
	 * 全更新，注意为Null的属生也会更新
	 */
	protected int resetAllTUserDemoInfo(TUserDemoInfo tUserDemoInfo) {
		tUserDemoInfo.setModifyTimestamp(System.currentTimeMillis());
		SqlSession session = dbContext.getWriteSession(dataSource);
		int ret = session.update("TUSERDEMOINFO.updateTUserDemoInfo", tUserDemoInfo);
		if (ret > 0) {
			dbContext.publishDataModifyEvent(dataSource, session.getConnection(), tUserDemoInfo, EventOperate.UPDATE,
					pkToString(tUserDemoInfo));
		}
		return ret;
	}

	/** 批量全更新 **/
	protected int resetBatchTUserDemoInfo(List<TUserDemoInfo> tUserDemoInfoList) {
		SqlSession session = dbContext.getBatchSession(dataSource);
		int i = 0;
		for (TUserDemoInfo tUserDemoInfo : tUserDemoInfoList) {
			tUserDemoInfo.setModifyTimestamp(System.currentTimeMillis());
			resetBatchTUserDemoInfo(session, tUserDemoInfo);
			i++;
		}
		session.flushStatements();
		return i;
	}

	private void resetBatchTUserDemoInfo(SqlSession session, TUserDemoInfo tUserDemoInfo) {
		int ret = session.update("TUSERDEMOINFO.updateTUserDemoInfo", tUserDemoInfo);
		if (ret > 0) {
			dbContext.publishDataModifyEvent(dataSource, session.getConnection(), tUserDemoInfo, EventOperate.UPDATE,
					pkToString(tUserDemoInfo));
		}
	}

	/**
	 * 根据对象主键部分更新字段 注意：属性为Null不会更新
	 */
	public int updatePartTUserDemoInfo(long userId, TUserDemoInfo tUserDemoInfo) {
		tUserDemoInfo.setUserId(userId);
		tUserDemoInfo.setModifyTimestamp(System.currentTimeMillis());
		ValidationUtil.validate(tUserDemoInfo, Modify.class);
		SqlSession session = dbContext.getWriteSession(dataSource);
		int ret = session.update("TUSERDEMOINFO.updatePartTUserDemoInfo", tUserDemoInfo);
		if (ret > 0) {
			dbContext.publishDataModifyEvent(dataSource, session.getConnection(), tUserDemoInfo,
					EventOperate.PART_MODIFY, pkToString(tUserDemoInfo));
		}
		return ret;
	}

	/**
	 * 部分更新,属性不为Null的会更新
	 */
	protected int updatePartTUserDemoInfo(TUserDemoInfo tUserDemoInfo) {
		tUserDemoInfo.setModifyTimestamp(System.currentTimeMillis());
		ValidationUtil.validate(tUserDemoInfo, Modify.class);
		SqlSession session = dbContext.getWriteSession(dataSource);
		int ret = session.update("TUSERDEMOINFO.updatePartTUserDemoInfo", tUserDemoInfo);
		if (ret > 0) {
			dbContext.publishDataModifyEvent(dataSource, session.getConnection(), tUserDemoInfo,
					EventOperate.PART_MODIFY, pkToString(tUserDemoInfo));
		}
		return ret;
	}

	/** 批量更新 **/
	protected int updatePartBatchTUserDemoInfo(List<TUserDemoInfo> tUserDemoInfoList) {
		SqlSession session = dbContext.getBatchSession(dataSource);
		int i = 0;
		for (TUserDemoInfo tUserDemoInfo : tUserDemoInfoList) {
			tUserDemoInfo.setModifyTimestamp(System.currentTimeMillis());
			ValidationUtil.validate(tUserDemoInfo, Modify.class);
			updatePartBatchTUserDemoInfo(dataSource, session, tUserDemoInfo);
			i++;
		}
		session.flushStatements();
		return i;
	}

	private void updatePartBatchTUserDemoInfo(String dataSource, SqlSession session, TUserDemoInfo tUserDemoInfo) {
		int ret = session.update("TUSERDEMOINFO.updatePartTUserDemoInfo", tUserDemoInfo);
		if (ret > 0) {
			dbContext.publishDataModifyEvent(dataSource, session.getConnection(), tUserDemoInfo,
					EventOperate.PART_MODIFY, pkToString(tUserDemoInfo));
		}
	}

	/**
	 * 根据主键按条件部分更新
	 * 
	 * @param List<Long>    userIds 主键列表
	 * @param TUserDemoInfo conditionTUserDemoInfo 旧值(条件)
	 * @param TUserDemoInfo newTUserDemoInfo 新值
	 */
	public int updatePartByIds(List<Long> userIds, TUserDemoInfoPage conditionTUserDemoInfo,
			TUserDemoInfo newTUserDemoInfo) {
		newTUserDemoInfo.setModifyTimestamp(System.currentTimeMillis());
		ValidationUtil.validate(newTUserDemoInfo, Modify.class);
		Map<String, Object> map = new HashMap<>();
		if (CollectionUtils.isEmpty(userIds)) {
			log.debug("userIds is empty!");
			return 0;
		}
		map.put("ids", userIds);
		if (conditionTUserDemoInfo != null) {
			conditionTUserDemoInfo.setUserId(null);
			map.put("oldObj", conditionTUserDemoInfo);
		}
		map.put("newObj", newTUserDemoInfo);
		SqlSession session = dbContext.getWriteSession(dataSource);
		int ret = session.update("TUSERDEMOINFO.updatePartTUserDemoInfoByIds", map);
		if (ret > 0) {
			TUserDemoInfo tUserDemoInfo = newTUserDemoInfo;
			for (Long userId : userIds) {
				tUserDemoInfo.setUserId(userId);
				dbContext.publishDataModifyEvent(dataSource, session.getConnection(), tUserDemoInfo,
						EventOperate.OTHER_MODIFY, pkToString(tUserDemoInfo));
			}
		}
		return ret;
	}

	/** 加载 **/
	public TUserDemoInfo queryTUserDemoInfo(long userId) {
		DBVariable invocationContext = (DBVariable) DBVariable.getThreadVariable("db");
		TUserDemoInfo localCache = (TUserDemoInfo) invocationContext.getLocalCachePojo(TUserDemoInfo.class,
				String.valueOf(userId));
		if (null != localCache) {
			return localCache;
		}
		if (isCachable() && !invocationContext.isDeletePojo(TUserDemoInfo.class, String.valueOf(userId))) {
			byte[] bytes = repository.getBytes(String.valueOf(userId));
			if (bytes != null) {
				return SerializerUtil.deserialize(bytes, TUserDemoInfo.class);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("queryTUserDemoInfo {} from db", userId);
		}
		TUserDemoInfoPage tUserDemoInfo = new TUserDemoInfoPage();
		tUserDemoInfo.setUserId(userId);
		SqlSession session = dbContext.getReadSession(dataSource);
		TUserDemoInfo retVal = session.selectOne("TUSERDEMOINFO.queryTUserDemoInfo", tUserDemoInfo);
		if (retVal != null) {
			dbContext.publishReadPojo(dataSource, retVal, String.valueOf(retVal.getUserId()));
		}
		return retVal;
	}

	/** 查询 **/
	protected List<TUserDemoInfo> listTUserDemoInfo(TUserDemoInfoPage tUserDemoInfo) {
		tUserDemoInfo.setPageSize(tUserDemoInfo.getPageSize() + 1);
		SqlSession session = dbContext.getReadSession(dataSource);
		List<TUserDemoInfo> list = session.selectList("TUSERDEMOINFO.listTUserDemoInfo", tUserDemoInfo);
		dbContext.publishReadList(dataSource, TUserDemoInfo.class, tUserDemoInfo, list);
		tUserDemoInfo.setPageSize(tUserDemoInfo.getPageSize() - 1);
		if (list.size() > tUserDemoInfo.getPageSize()) {
			tUserDemoInfo.setNextPage(true);
			list.remove((int) tUserDemoInfo.getPageSize());
		} else {
			tUserDemoInfo.setNextPage(false);
		}
		return list;
	}

	/** 查询数量 **/
	protected long countTUserDemoInfo(TUserDemoInfoPage tUserDemoInfo) {
		SqlSession session = dbContext.getReadSession(dataSource);
		long count = session.selectOne("TUSERDEMOINFO.countTUserDemoInfo", tUserDemoInfo);
		dbContext.publishReadCount(dataSource, TUserDemoInfo.class, tUserDemoInfo, count);
		return count;
	}

	/** 分页查询 **/
	public PagedResult<TUserDemoInfo> pagedResultOfListTUserDemoInfo(TUserDemoInfoPage tUserDemoInfo) {
		PagedResult<TUserDemoInfo> result = new PagedResult<>();
		result.setList(listTUserDemoInfo(tUserDemoInfo));
		result.setHasNextPage(tUserDemoInfo.isNextPage());
		result.setCount(countTUserDemoInfo(tUserDemoInfo));
		return result;
	}

	public List<TUserDemoInfo> listTUserDemoInfoByIdsFromDB(List<Long> userIds) {
		if (CollectionUtils.isEmpty(userIds)) {
			return new ArrayList<TUserDemoInfo>();
		}
		SqlSession session = dbContext.getReadSession(dataSource);
		List<TUserDemoInfo> list = session.selectList("TUSERDEMOINFO.listTUserDemoInfoByIds", userIds);
		dbContext.publishReadList(dataSource, TUserDemoInfo.class, userIds, list);
		return list;
	}

	public List<TUserDemoInfo> listTUserDemoInfoByIds(List<Long> userIds) {
		if (CollectionUtils.isEmpty(userIds)) {
			return new ArrayList<TUserDemoInfo>();
		}
		DBVariable invocationContext = (DBVariable) DBVariable.getThreadVariable("db");
		List<Long> hitList = new ArrayList<>();
		List<TUserDemoInfo> localCacheList = new ArrayList<>();
		for (Long userId : userIds) {
			TUserDemoInfo localCache = (TUserDemoInfo) invocationContext.getLocalCachePojo(TUserDemoInfo.class,
					String.valueOf(userId));
			if (null != localCache) {
				hitList.add(userId);
				localCacheList.add(localCache);
			}
		}
		userIds.removeAll(hitList);
		// 如果不走缓存，就直接从数据库中取
		if (!this.isCachable()) {
			List<TUserDemoInfo> dbList = listTUserDemoInfoByIdsFromDB(userIds);
			dbList.addAll(localCacheList);
			return dbList;
		}
		List<byte[]> values = repository.getMultiByteValue(ListUtils.list2ByteList(userIds));
		// redis访问失败，就从数据库中读
		if (null == values) {
			List<TUserDemoInfo> dbList = listTUserDemoInfoByIdsFromDB(userIds);
			dbList.addAll(localCacheList);
			return dbList;
		}
		List<TUserDemoInfo> retList = new ArrayList<TUserDemoInfo>(userIds.size());
		List<Long> notCachedidList = new ArrayList<>(userIds.size());
		// 缓存中有的对象，就直接从缓存中获取，不存在的对象，就将id保存起来。
		for (int i = 0; i < values.size(); i++) {
			byte[] value = values.get(i);
			if (null == value) {
				notCachedidList.add(userIds.get(i));
			} else {
				retList.add(SerializerUtil.deserialize(value, TUserDemoInfo.class));
			}
		}
		// 全部都在缓存中
		if (notCachedidList.isEmpty()) {
			retList.addAll(localCacheList);
			return retList;
		}

		// 从数据库中获取缓存中没有的对象
		List<TUserDemoInfo> objs = this.listTUserDemoInfoByIdsFromDB(notCachedidList);
		// 根据主键查询不可能存在重复的对象，所以不需要去重
		retList.addAll(objs);
		retList.addAll(localCacheList);
		return retList;
	}

	/*
	 * 加载指定条件的所有id
	 * 
	 * @param TUserDemoInfoPage tUserDemoInfo
	 */
	public List<Long> listIds(TUserDemoInfoPage tUserDemoInfo) {
		tUserDemoInfo.setPageSize(tUserDemoInfo.getPageSize() + 1);
		SqlSession session = dbContext.getReadSession(dataSource);
		List<Long> list = session.selectList("TUSERDEMOINFO.listIds", tUserDemoInfo);
		dbContext.publishReadList(dataSource, TUserDemoInfo.class, tUserDemoInfo, list);
		tUserDemoInfo.setPageSize(tUserDemoInfo.getPageSize() - 1);
		if (list.size() > tUserDemoInfo.getPageSize()) {
			tUserDemoInfo.setNextPage(true);
			list.remove((int) tUserDemoInfo.getPageSize());
		} else {
			tUserDemoInfo.setNextPage(false);
		}
		return list;
	}

	/**
	 * 获取最大主键
	 */
	public long getMaxSequence() {
		SqlSession session = dbContext.getReadSession(dataSource);
		long maxSequence = session.selectOne("TUSERDEMOINFO.selectMaxTUserDemoInfoSequence");
		return maxSequence;
	}
}
