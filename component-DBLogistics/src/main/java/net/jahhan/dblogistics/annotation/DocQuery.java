package net.jahhan.dblogistics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocQuery {

	public String value() default "";

	public String sort() default "";
	
	public String projection() default "";
	
	public String skip() default "";
	
	public String limit() default "";
	
	public boolean useNeo() default true;
	
	public boolean writeQuery() default false;
	
	public String[] aggregate() default {};
}