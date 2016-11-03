package net.jahhan.dubbo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.constant.enumeration.DBConnectionType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBConnect {
	/**
	 * 数据库连接方式
	 */
	public DBConnectionType value() default DBConnectionType.NONE;
}