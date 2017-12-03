package net.jahhan.init.module;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.guice.module.SpringModule;

import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = true)
public class GuiceSpringModule extends SpringModule {

	public GuiceSpringModule(ApplicationContext context) {
		super(context);
	}

	public GuiceSpringModule() {
		this(new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml", "daoContext.xml" }));
	}
}
