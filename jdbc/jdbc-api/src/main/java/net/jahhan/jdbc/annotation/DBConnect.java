package net.jahhan.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.jdbc.constant.enumeration.DBConnectLevel;
import net.jahhan.jdbc.constant.enumeration.DBConnectStrategy;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBConnect {
	public static DBConnectStrategy defaultDBConnectStrategy = DBConnectStrategy.UPDATA;

	/**
	 * 数据源
	 */
	public String dataSource() default "";

	/**
	 * 连接策略
	 */
	public DBConnectStrategy value() default DBConnectStrategy.UPDATA;

	/**
	 * 连接等级
	 */
	public DBConnectLevel level() default DBConnectLevel.NONE;

	/**
	 * 是否开启事务
	 */
	public boolean transaction() default true;
}