package net.jahhan.init;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.guice.annotation.EnableGuiceModules;

@EnableGuiceModules
@Configuration
@ImportResource("classpath:spring-*.xml")
public class SpringConfiguration {
}
