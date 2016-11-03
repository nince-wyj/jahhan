package net.jahhan.web;

import java.io.Serializable;
import java.util.List;

public class UserEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private String userName;

	private Long userId;

	private Long loginRoleType = 1l;
	
	private Long loginRoleId;
	
	private List<Long> roleIds;

	private int appType;

	private String addr;

	private String pageAuthorityMD5;

	private String actAuthorityMD5;
	
	private String elementAuthorityMD5;
	
	private String lat;
	
	private String lon;
	
	private Long cityId;
	
	private Long regionId;
	
	private String token;

	public Long getLoginRoleId() {
		return loginRoleId;
	}

	public void setLoginRoleId(Long loginRoleId) {
		this.loginRoleId = loginRoleId;
	}

	public List<Long> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Long> roleIds) {
		this.roleIds = roleIds;
	}

	public Long getRegionId() {
		return regionId;
	}

	public void setRegionId(Long regionId) {
		this.regionId = regionId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public Long getCityId() {
		return cityId;
	}

	public void setCityId(Long cityId) {
		this.cityId = cityId;
	}

	public Long getLoginRoleType() {
		return loginRoleType;
	}

	public void setLoginRoleType(Long loginRoleType) {
		this.loginRoleType = loginRoleType;
	}

	public String getPageAuthorityMD5() {
		return pageAuthorityMD5;
	}

	public void setPageAuthorityMD5(String pageAuthorityMD5) {
		this.pageAuthorityMD5 = pageAuthorityMD5;
	}

	public String getActAuthorityMD5() {
		return actAuthorityMD5;
	}

	public void setActAuthorityMD5(String actAuthorityMD5) {
		this.actAuthorityMD5 = actAuthorityMD5;
	}
	
	public String getElementAuthorityMD5() {
		return elementAuthorityMD5;
	}

	public void setElementAuthorityMD5(String elementAuthorityMD5) {
		this.elementAuthorityMD5 = elementAuthorityMD5;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Long getUserId() {
		return userId;
	}

	public int getAppType() {
		return appType;
	}

	public void setAppType(int appType) {
		this.appType = appType;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
