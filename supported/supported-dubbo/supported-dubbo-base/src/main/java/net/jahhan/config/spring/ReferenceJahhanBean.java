package net.jahhan.config.spring;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.ReferenceBean;

public class ReferenceJahhanBean<T> extends ReferenceBean<T> {
	private static final long serialVersionUID = -3990940698663809813L;

	public ReferenceJahhanBean() {
		super();
	}

	public ReferenceJahhanBean(Reference reference) {
		super(reference);
	}
}
