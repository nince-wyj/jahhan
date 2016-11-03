package net.jahhan.cache.demand.impl;

import com.alibaba.dubbo.common.utils.ConfigUtils;

import net.jahhan.demand.HostDemand;
import net.jahhan.dubbo.cache.HostCache;
import net.jahhan.utils.PropertiesUtil;

public class HostDemandImpl implements HostDemand {

	@Override
	public String getThisHostAndPort() {
		return ConfigUtils.getProperty("dubbo.protocol.host") + ":" + HostCache.getInstance().getPort();
	}

	@Override
	public String getThisHost() {
		return ConfigUtils.getProperty("dubbo.protocol.host");
	}

	@Override
	public int getPost() {
		return HostCache.getInstance().getPort();
	}

	@Override
	public String getHostType() {
		return PropertiesUtil.get("dubbo", "dubbo.application.name");
	}

}
