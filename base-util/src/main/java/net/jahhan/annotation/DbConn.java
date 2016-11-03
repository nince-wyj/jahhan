package net.jahhan.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.constant.enumeration.DBConnectionType;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface DbConn {
	public DBConnectionType value() default DBConnectionType.NONE;

	public boolean transaction() default true;
}
