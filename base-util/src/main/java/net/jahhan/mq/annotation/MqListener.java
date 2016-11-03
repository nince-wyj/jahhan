package net.jahhan.mq.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.mq.MqMode;
import net.jahhan.mq.Topic;

/**
 * 消息队列监听器注解
 * 
 * @author nince
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MqListener {
	public Topic topicType() default Topic.SYSTEM;

	public String topicName() default "";

	public DBConnectionType conn() default DBConnectionType.NONE;

	public MqMode mqMode() default MqMode.PubSub;
}