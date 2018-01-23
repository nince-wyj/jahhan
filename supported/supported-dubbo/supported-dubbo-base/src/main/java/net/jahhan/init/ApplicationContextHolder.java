package net.jahhan.init;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationContextHolder {
	private static AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
			SpringConfiguration.class);
	static{
//		context.removeBeanDefinition("Logger");
	}
	public static AnnotationConfigApplicationContext getContext(){
		return context;
	}
}
