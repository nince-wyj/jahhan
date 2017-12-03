package com.frameworkx.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionEvent extends DBEvent {

	private static final long serialVersionUID = -1239699492387578131L;

	protected Logger logger = LoggerFactory.getLogger("event.CollectionEvent");

	private Object para;

	public CollectionEvent(Object source, String dataSource, Object para, String type) {
		super(source, dataSource, type, EventOperate.LIST, null);
		this.para = para;
	}

	/**
	 * 获取列表时所用到的参数，大多数情况下是*Page类型
	 * 
	 * @return
	 */
	public Object getPara() {
		return para;
	}

}