package net.jahhan.init;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationContextHolder {
	private static AnnotationConfigApplicationContext context;

	static {
		// context.removeBeanDefinition("Logger");
	}

	public static AnnotationConfigApplicationContext getContext() {
		if (null == context) {
			context = new AnnotationConfigApplicationContext(SpringConfiguration.class);
		}
		return context;
	}
}
