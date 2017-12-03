package com.frameworkx.jdbc;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

public class SuperPojo<T> implements Serializable{
	private static final long serialVersionUID = 10000000L;

	@SuppressWarnings("unchecked")
	public T clone(){
		return 	(T) SerializationUtils.clone(this);
	}
}
