package com.frameworkx.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountEvent extends DBEvent {

	private static final long serialVersionUID = 34524134243445L;

	protected Logger logger = LoggerFactory.getLogger("event.CountEvent");

	private Object para;

	public CountEvent(String dataSource, Long count, String type, Object para) {
		super(count, dataSource, type, EventOperate.COUNT, null);
		this.para = para;
	}

	public Object getPara() {
		return para;
	}

}