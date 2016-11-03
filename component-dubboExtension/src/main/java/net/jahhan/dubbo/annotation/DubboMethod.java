package net.jahhan.dubbo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.jahhan.dubbo.enumeration.ClusterTypeEnum;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DubboMethod {
	/**
	 * 方法调用超时时间(毫秒)
	 */
	public int timeout() default -1;

	/**
	 * 远程服务调用重试次数，不包括第一次调用，不需要重试请设为0
	 */
	public int retries() default -1;

	/**
	 * 负载均衡策略，可选值：random,roundrobin,leastactive，分别表示：随机，轮循，最少活跃调用
	 */
	public String loadbalance() default "";

	/**
	 * 是否异步执行，不可靠异步，只是忽略返回值，不阻塞执行线程
	 */
	public boolean async() default false;

	/**
	 * 是否等待消息发出：(异步总是不等待返回)
	 */
	public boolean sent() default true;

	/**
	 * 每服务消费者最大并发调用限制
	 */
	public int actives() default -1;

	/**
	 * 每服务每方法最大使用线程数限制- -，此属性只在<dubbo:method>作为<dubbo:service>子标签时有效
	 */
	public int executes() default -1;

	/**
	 * 服务方法是否过时，此属性只在<dubbo:method>作为<dubbo:service>子标签时有效
	 */
	public boolean deprecated() default false;

	/**
	 * 设置true 该接口上的所有方法使用同一个provider.如果需要更复杂的规则，请使用用路由
	 */
	public boolean sticky() default false;

	/**
	 * 方法调用是否需要返回值,async设置为true时才生效，如果设置为true，则返回future，或回调onreturn等方法，
	 * 如果设置为false，则请求发送成功后直接返回Null
	 */
	public boolean isReturn() default true;

	/**
	 * 方法执行前拦截(类全名)
	 */
	public String oninvokeintance() default "";

	/**
	 * 方法执行前拦截(方法名)
	 */
	public String oninvoke() default "";

	/**
	 * 方法执行返回后拦截(类全名)
	 */
	public String onreturnintance() default "";

	/**
	 * 方法执行返回后拦截(方法名)
	 */
	public String onreturn() default "";

	/**
	 * 方法执行有异常拦截(类全名)
	 */
	public String onthrowintance() default "";

	/**
	 * 方法执行有异常拦截(方法名)
	 */
	public String onthrow() default "";

	/**
	 * 以调用参数为key，缓存返回结果，可选：lru, threadlocal, jcache等
	 */
	public String cache() default "";

	/**
	 * 是否启用JSR303标准注解验证，如果启用，将对方法参数上的注解进行校验
	 */
	public String validation() default "";

	/**
	 * 合并器
	 */
	public String merger() default "";

	/**
	 * 服务接口的失败mock实现类名
	 */
	public String mock() default "";
}