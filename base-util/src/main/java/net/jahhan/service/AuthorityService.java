package net.jahhan.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jahhan.web.UserEntity;

public interface AuthorityService {

	/**
	 * 获取用户权限
	 * 
	 * @param userID
	 * @return
	 */
	public List<Long> getUserAuth(Long userID);

	/**
	 * 获取用户页面的特殊权限
	 * 
	 * @param userID
	 * @return
	 */
	public Set<String> getUserSpecialPageAuthority(Long userID);

	/**
	 * 获取用户操作的特殊权限
	 * 
	 * @param userID
	 * @return
	 */
	public Set<String> getUserSpecialActAuthority(Long userID);

	/**
	 * 获取用户页面元素的特殊权限
	 * 
	 * @param userID
	 * @return
	 */
	public Set<String> getUserSpecialElementAuthority(Long userID);

	/**
	 * 获取权限操作的权限
	 * 
	 * @param roleIDs
	 * @return
	 */
	public Set<String> getAuthActAuthority(List<Long> authIDs);

	/**
	 * 获取权限页面的权限
	 * 
	 * @param roleIDs
	 * @return
	 */
	public Set<String> getAuthPageAuthority(List<Long> authIDs);

	/**
	 * 获取权限页面元素的权限
	 * 
	 * @param roleIDs
	 * @return
	 */
	public Set<String> getAuthElementAuthority(List<Long> authIDs);

	/**
	 * 获取无权限控制的页面
	 * 
	 * @param roleIDs
	 * @return
	 */
	public Set<String> getPagesWithoutAuthority();

	/**
	 * 获取页面名称及其路径的键值对
	 * 
	 * @param page
	 * @return
	 */
	public Map<String, String> getPageUrlMap();

	/**
	 * 接口信息保存到数据库
	 * 
	 * @param appType
	 * @param actName
	 * @return
	 */
	public boolean actSave(int appType, String actName, String description);

	/**
	 * 设置用户的权限md5值
	 * @param userEntity
	 */
	public void setAuthorityMD5(UserEntity userEntity);
}
