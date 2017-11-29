package net.jahhan.jdbc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Transaction {
	/**
	 * 数据源
	 */
	public String[] value() default "";
	/**
	 * 是否响应全局锁
	 */
	public boolean globalRespond() default true;
}
