package net.jahhan.demo.dao.abstrDao;

import java.sql.SQLException;
import java.util.List;

import net.jahhan.demo.pojo.TUserDemoInfo;
import net.jahhan.demo.pojo.page.TUserDemoInfoPage;
import net.jahhan.jdbc.pojo.page.PagedResult;

/*
 * 自动生成,开发人员请勿修改.
 * 
 * @author code-generate-service
 */
public interface AbstrTUserDemoInfoDao
{
   /**
	* 增加 
	*/
	int addTUserDemoInfo(TUserDemoInfo tUserDemoInfo);
	
	/**
	* 批量增加 
	*/
	int addBatchTUserDemoInfo(List<TUserDemoInfo> tUserDemoInfoList);
	
   /**
	* 根据对象主键更新对象所有字段
	* 注意：属性为Null也会更新置空 
	*/
	int resetAllTUserDemoInfo(long userId,TUserDemoInfo tUserDemoInfo);
	
	
   /**
	* 根据对象主键部分更新字段
	* 注意：属性为Null不会更新
	*/	
	int updatePartTUserDemoInfo(long userId,TUserDemoInfo tUserDemoInfo);	
	
	/**
	 * 根据主键按条件部分更新
	 * @param List<Long>  userIds 主键列表
	 * @param TUserDemoInfo conditionTUserDemoInfo 旧值
	 * @param TUserDemoInfo newTUserDemoInfo 新值
	 */
	int updatePartByIds(List<Long> userIds,TUserDemoInfoPage conditionTUserDemoInfo,TUserDemoInfo newTUserDemoInfo);
	
   /**
	* 删除 
	*/
	int delTUserDemoInfo(long userId);
	
	/** 
	 * 加载
	 * @throws SQLException
	 */
	TUserDemoInfo queryTUserDemoInfo(long userId);
	
	/**
	 * 根据主键Id列出对象，不保证记录的顺序
	 * @param ids 以逗号分隔的id列表
	 * @return
	 */
	List<TUserDemoInfo> listTUserDemoInfoByIds(List<Long> userIds);
	
	/**
	 * 加载指定条件的所有id
	 */
	List<Long> listIds(TUserDemoInfoPage tUserDemoInfo);
	
	/** 分页查询 **/
	public PagedResult<TUserDemoInfo> pagedResultOfListTUserDemoInfo(TUserDemoInfoPage tUserDemoInfo);
	
	
	/**
	 * 批量删除
	 */	
	int delByIds(List<Long> userIds);
	
	/**
	 * 获取最大主键
	 */		
	long getMaxSequence();
}
