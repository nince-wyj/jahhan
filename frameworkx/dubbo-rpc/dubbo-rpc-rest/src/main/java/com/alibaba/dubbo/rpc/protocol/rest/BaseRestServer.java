package com.alibaba.dubbo.rpc.protocol.rest;

import java.util.List;

import org.jboss.resteasy.spi.ResteasyDeployment;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;

import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.common.extension.utils.StringUtils;

public abstract class BaseRestServer implements RestServer {

	public void start(URL url) {
		getDeployment().getMediaTypeMappings().put("json", "application/json");
		getDeployment().getMediaTypeMappings().put("xml", "text/xml");

		String[] packages = PackageUtil.packages("rest.filter");
		List<String> classNameList = new ClassScaner().parse(packages);
		for (String className : classNameList) {
			getDeployment().getProviderClasses().add(className);
		}
		loadProviders(url.getParameter(Constants.EXTENSION_KEY, ""));

		doStart(url);
	}

	public void deploy(Class resourceDef, Object resourceInstance, String contextPath) {
		if (StringUtils.isEmpty(contextPath)) {
			getDeployment().getRegistry().addResourceFactory(new DubboResourceFactory(resourceInstance, resourceDef));
		} else {
			getDeployment().getRegistry().addResourceFactory(new DubboResourceFactory(resourceInstance, resourceDef),
					contextPath);
		}
	}

	public void undeploy(Class resourceDef) {
		getDeployment().getRegistry().removeRegistrations(resourceDef);
	}

	protected void loadProviders(String value) {
		for (String clazz : Constants.COMMA_SPLIT_PATTERN.split(value)) {
			if (!StringUtils.isEmpty(clazz)) {
				getDeployment().getProviderClasses().add(clazz.trim());
			}
		}
	}

	protected abstract ResteasyDeployment getDeployment();

	protected abstract void doStart(URL url);
}
