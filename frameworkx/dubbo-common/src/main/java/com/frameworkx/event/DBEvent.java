package com.frameworkx.event;

import java.util.EventObject;

import lombok.Getter;

public class DBEvent extends EventObject {
	private static final long serialVersionUID = -3786807208667396149L;

	/**
	 * 数据源，非空
	 */
	@Getter
	protected String dataSource;
	/**
	 * 表名，非空
	 */
	@Getter
	private String type;

	/**
	 * 操作类型，非空
	 */
	@Getter
	private String operate;

	/**
	 * 主键id，可以为空
	 */
	@Getter
	private String id;

	/**
	 * @param source
	 * @param type
	 *            表名，非空
	 * @param operate
	 *            操作，非空
	 * @param id
	 */
	public DBEvent(Object source, String dataSource, String type, String operate, String id) {
		super(source);
		this.dataSource = dataSource;
		this.type = type;
		this.operate = operate;
		this.id = id;
	}

	@Override
	public String toString() {
		return "DBEvent [dataSource=" + dataSource + ", type=" + type + ",operate=" + operate + ", id=" + id + "]";
	}
}
