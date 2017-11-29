package net.jahhan.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 本注解在接口入口注解为定义接口类型信息，在服务层定义为开启数据库连接
 * @author Administrator
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBConnections {
	/**
	 * 数据连接
	 */
	public DBConnect[] value() default {};

}