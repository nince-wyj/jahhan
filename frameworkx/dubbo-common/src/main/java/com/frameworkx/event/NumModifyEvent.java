package com.frameworkx.event;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 金额或数量改变的event
 */
public class NumModifyEvent extends DBEvent {

	private static final long serialVersionUID = -1239699492387578131L;

	protected Logger logger = LoggerFactory.getLogger("event.NumModifyEvent");

	private String field;

	public NumModifyEvent(Object source, String dataSource, String type, String operate, String id, String field) {
		super(source, dataSource, type, operate, id);
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public static NumModifyEvent createAdd(String dataSource, String type, String id, String field, Number num) {
		if (StringUtils.isEmpty(type) || StringUtils.isEmpty(field) || num == null || StringUtils.isEmpty(id)) {
			return null;
		}
		return new NumModifyEvent(num, dataSource, type, EventOperate.ADDNUM, id, field);
	}

}