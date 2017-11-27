package net.jahhan.init.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import net.jahhan.annotation.SPI;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.init.InitAnnocation;
import net.jahhan.utils.ClassScaner;
import net.jahhan.utils.PropertiesUtil;

@InitAnnocation(isLazy = false, initSequence = 1000)
public class PluginSetModule extends AbstractModule {
	private final Logger logger = LoggerFactory.getInstance().getLogger(PluginSetModule.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void configure() {
		String[] packages = new String[] { SysConfiguration.getCompanyName() + ".plugins", "net.jahhan.plugins" };
		List<String> classNameList = new ClassScaner().parse(packages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Properties properties = PropertiesUtil.getProperties("pluginsInit");
		Map<Class, List<Class>> unSetMap = new HashMap<>();
		List<Class> settedList = new ArrayList<>();
		for (String className : classNameList) {
			try {
				Class<?> clazz = classLoader.loadClass(className);
				if (clazz.isAnnotationPresent(SPI.class)) {
					SPI spi = clazz.getAnnotation(SPI.class);
					Class<?>[] interfaces = clazz.getInterfaces();
					Class interfaceClass = interfaces[0];
					SPI interfaceSPI = (SPI) interfaceClass.getAnnotation(SPI.class);
					bind(interfaceClass).annotatedWith(Names.named(spi.value())).to(clazz);

					String defaultPlugin = properties.getProperty(interfaceClass.getName());
					if (null == defaultPlugin) {
						defaultPlugin = interfaceSPI.value();
					}
					if (spi.value().equals(defaultPlugin)) {
						bind(interfaceClass).to(clazz);
						settedList.add(clazz);
					} else {
						List<Class> list = unSetMap.get(interfaceClass);
						if (null == list) {
							list = new ArrayList<>();
							list.add(clazz);
							unSetMap.put(interfaceClass, list);
						} else {
							list.add(clazz);
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

		}
		Iterator<Class> iterator = unSetMap.keySet().iterator();
		while (iterator.hasNext()) {
			Class next = iterator.next();
			if (settedList.contains(next)) {
				continue;
			}
			bind(unSetMap.get(next).get(0)).to(next);
		}
	}
}
