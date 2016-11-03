package net.jahhan.web.action.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.constant.enumeration.DBLogisticsConnectionType;
import net.jahhan.constant.enumeration.FastBackEnum;
import net.jahhan.constant.enumeration.LoginEnum;
import net.jahhan.constant.enumeration.RequestMethodEnum;
import net.jahhan.constant.enumeration.ResponseTypeEnum;

/**
 * 接口参数描述
 * 
 * @author nince
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionService {
	public String act();

	public String[] importantParameters() default {};

	public String[] minorParameters() default {};

	public LoginEnum requireLogin() default LoginEnum.NO;

	public CryptEnum requestEncrypt() default CryptEnum.PLAIN;

	public CryptEnum signEncrypt() default CryptEnum.PLAIN;

	public String[] returnParameters() default {};

	public CryptEnum responseEncrypt() default CryptEnum.PLAIN;

	public ResponseTypeEnum responseType() default ResponseTypeEnum.JSON;

	public String description() default "";

	public boolean withoutAuth() default false;

	public Field[] validateParameters() default {};

	public DBConnectionType dbConnType() default DBConnectionType.READ;

	public RequestMethodEnum requestMethod() default RequestMethodEnum.JSON;

	public Class<?> referenceObject() default void.class;

	public boolean async() default false;

	public String version() default "";

	public boolean fastBack() default false;

	public int blockTime() default 1;

	public FastBackEnum fastBackType() default FastBackEnum.SESSION;

	public boolean fastBackFail() default true;
	
	public DBLogisticsConnectionType dbLogistics() default DBLogisticsConnectionType.NONE;
}
