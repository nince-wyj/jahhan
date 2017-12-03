package com.frameworkx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.frameworkx.constant.enumeration.RPCTypeEnum;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DubboInterface {
	/**
	 * rpc类型
	 */
	public RPCTypeEnum rpcType() default RPCTypeEnum.CLOUD;
	
	/**
	 * 路由目标 clusterType为direct时有效
	 */
	public String target() default "";
}