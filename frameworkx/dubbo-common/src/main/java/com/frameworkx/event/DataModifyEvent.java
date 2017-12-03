package com.frameworkx.event;

import com.frameworkx.jdbc.SuperPojo;

/**
 * 数据变更的event
 */
public class DataModifyEvent extends DBEvent {

	public DataModifyEvent(SuperPojo<?> source, String dataSource, String type, String operate, String id) {
		super(source.clone(), dataSource, type, operate, id);
	}

	private static final long serialVersionUID = -8607165576941794121L;

}
