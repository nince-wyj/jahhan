package net.jahhan.dubbo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.dubbo.enumeration.ClusterTypeEnum;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DubboInterface {
	/**
	 * 路由类型
	 */
	public ClusterTypeEnum clusterType() default ClusterTypeEnum.FAILBACK;
	
	/**
	 * 路由目标 clusterType为direct时有效
	 */
	public String clusterTarget() default "";
}