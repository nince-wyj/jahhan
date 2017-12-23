package net.jahhan.demo.dao.impl;

import net.jahhan.demo.dao.abstrimpl.AbstrTUserDemoInfoImpl;
import net.jahhan.demo.pojo.TUserDemoInfo;

/*
 * 开发人员在此实现接口方法
 *
 * @author code-generate-service
 */
public class TUserDemoInfoDaoImpl  extends AbstrTUserDemoInfoImpl {
	
	@Override
	protected boolean isCachable() {
		return false;//如果需要使用缓存，请使用TUserDemoInfoDaoCache
	}
}