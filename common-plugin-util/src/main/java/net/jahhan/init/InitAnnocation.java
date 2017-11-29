package net.jahhan.init;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启动初始化注解
 * @author nince
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitAnnocation {
	// 必要参数，如果为true，则在其他初始化器都完成后启动
    public boolean isLazy() default false;
    // 只有isLazy为false时才生效，初始化将按照数值从小到大顺序进行初始化
    public int initSequence() default 9999;
    // web才需要启动的初始化
    public boolean onlyWeb() default false;
    // 初始化结束等待
    public boolean initOverWait() default true;
}