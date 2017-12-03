package net.jahhan.sdk.authenticationcenter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.sdk.authenticationcenter.constant.RequestModeType;

/**
 * 请求模式
 * 
 * @author Administrator
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMode {
	public RequestModeType value() default RequestModeType.COMMON;

}