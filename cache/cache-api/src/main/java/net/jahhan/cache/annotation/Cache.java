package net.jahhan.cache.annotation;

import net.jahhan.cache.annotation.enumeration.FastBackEnum;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

	/**是否快速失败*/
	public boolean fastBackFail() default false;

	/**限制时间，即多少时间内快速返回失败，默认值：1，默认时间单位：TimeUnit.SECONDS*/
	public int blockTime() default 1;

	/**blockTime的时间单位，默认时间单位：TimeUnit.SECONDS*/
	public TimeUnit blockTimeUnit() default TimeUnit.SECONDS;

	public FastBackEnum fastBackType() default FastBackEnum.ALL;

	/**快速失败返回的消息，默认：快速返回失败*/
	public String fastBackFailMessage() default "快速返回失败";

	/**
	 * 是否自定义key，只针对快速返回和快速失败有效，默认值：false
	 */
	public boolean isCustomCacheKey() default false;

	/** 参数序号数组，会取对应的参数值的字符串组成自定义缓存key
	 * (1)isCustomCacheKey值为true时，该字段配置才有效
	 * (2)没有配置值时不会取对应的参数值的字符串组成自定义缓存key，即对调用该接口的所有请求都生效
	 * */
	public int[] argumentIndexNumbers() default {};

	/**
	 * 自定义key的创建类，返回的值作为自定义缓存key的一部分，
	 * 配置的Class必须实现CustomCacheKeyCreater接口，否则配置无效,忽略该配置
	 * 有指定该类的话会忽略argumentIndexNumbers配置,若创建的key为null则该配置无效，将忽略该配置
	 */
	public Class<?> customCacheKeyCreaterClass() default void.class;
}
