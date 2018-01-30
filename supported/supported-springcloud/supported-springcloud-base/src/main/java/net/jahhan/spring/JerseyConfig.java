package net.jahhan.spring;

import java.util.List;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;

@Component
@ApplicationPath("/service")
public class JerseyConfig extends ResourceConfig {
	public JerseyConfig() {
		String[] packages = PackageUtil.packages("controller");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		List<String> classNameList = new ClassScaner().parse(packages);
		for (String className : classNameList) {
			try {
				Class<?> scanClass = classLoader.loadClass(className);
				RestController restController = scanClass.getAnnotation(RestController.class);
				Controller controller = scanClass.getAnnotation(Controller.class);
				Component component = scanClass.getAnnotation(Component.class);
				if (null != controller || null != restController || null != component) {
					register(scanClass);
				}
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		String[] filterPackages = PackageUtil.packages("rest.filter");
		List<String> filterClassNameList = new ClassScaner().parse(filterPackages);
		for (String className : filterClassNameList) {
			try {
				Class<?> scanClass = classLoader.loadClass(className);
				register(scanClass);
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}
}
