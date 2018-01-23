package net.jahhan.init;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.guice.annotation.EnableGuiceModules;
import org.springframework.guice.annotation.GuiceModule;

//@EnableGuiceModules
@Configuration
//@GuiceModule
@ImportResource("classpath:spring-*.xml")
public class SpringConfiguration{
}
