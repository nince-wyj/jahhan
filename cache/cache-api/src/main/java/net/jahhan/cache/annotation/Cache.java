package net.jahhan.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.cache.annotation.enumeration.FastBackEnum;

@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
	public boolean fastBackFail() default false;

	public int blockTime() default 1;

	public FastBackEnum fastBackType() default FastBackEnum.ALL;
}
