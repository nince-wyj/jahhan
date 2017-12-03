package net.jahhan.sdk.authenticationcenter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.sdk.authenticationcenter.constant.AuthorizationType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorization {
	public AuthorizationType[] value() default { AuthorizationType.USER };
}