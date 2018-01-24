package net.jahhan.config.spring;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ServiceBean;

import net.jahhan.config.ServiceImplCache;

public class ServiceJahhanBean<T> extends ServiceBean<T> {

	private static final long serialVersionUID = 5510156846006995575L;

	public ServiceJahhanBean() {
		super();
	}

	public ServiceJahhanBean(Service service) {
		super(service);
	}

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		ServiceImplCache.getInstance().regist(getInterface(), getRef().getClass());
	}
}
