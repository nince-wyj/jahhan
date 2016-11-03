package net.jahhan.init.module;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.slf4j.Logger;

import com.google.inject.AbstractModule;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import net.jahhan.dblogistics.SuperRepository;
import net.jahhan.dblogistics.annotation.DBRepository;
import net.jahhan.dblogistics.annotation.DocQuery;
import net.jahhan.dblogistics.annotation.NeoQuery;
import net.jahhan.dblogistics.constant.DBLogisticsConf;
import net.jahhan.dblogistics.entity.SuperEntity;
import net.jahhan.dblogistics.repository.RepositoryProviderHelper;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.init.InitAnnocation;
import net.jahhan.init.RepositoryPovider;
import net.jahhan.utils.ClassScaner;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

@SuppressWarnings("restriction")
@InitAnnocation(isLazy = false, initSequence = 3010)
public class RepositoryModule extends AbstractModule {
	private final Logger logger = LoggerFactory.getInstance().getLogger(RepositoryModule.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void configure() {
		String[] packages = new String[] { "net.jahhan.relationship.repository" };
		List<String> classNameList = new ClassScaner().parse(packages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Configuration cfg = new Configuration();
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		for (String className : classNameList) {
			try {
				Class<?> clazz = classLoader.loadClass(className);

				if (clazz.isAnnotationPresent(DBRepository.class) && SuperRepository.class.isAssignableFrom(clazz)) {
					Class<SuperRepository> dao = (Class<SuperRepository>) Class.forName(className);

					Type[] genericInterfaces = clazz.getGenericInterfaces();
					Type t = genericInterfaces[0];
					ParameterizedTypeImpl pt = (ParameterizedTypeImpl) t;
					Type[] actualTypeArguments = pt.getActualTypeArguments();
					Type type = actualTypeArguments[0];
					Class<? extends SuperEntity> entityClass = (Class<? extends SuperEntity>) Class
							.forName(type.getTypeName());
					RepositoryProviderHelper.getInstance().regist(entityClass);
					bind(dao).toProvider(new RepositoryPovider((Class<? extends SuperRepository>) clazz, entityClass));

					Method[] declaredMethods = clazz.getDeclaredMethods();
					for (Method method : declaredMethods) {
						DocQuery docQuery = method.getAnnotation(DocQuery.class);
						String classSimpleName = entityClass.getSimpleName();
						if (null != docQuery) {
							String value = docQuery.value();
							if (!value.equals("")) {
								stringLoader.putTemplate(classSimpleName + "." + method.getName() + ".doc.value",
										value);
							}
							String projection = docQuery.projection();
							if (!projection.equals("")) {
								stringLoader.putTemplate(classSimpleName + "." + method.getName() + ".doc.projection",
										projection);
							}
							String skip = docQuery.skip();
							if (!skip.equals("")) {
								stringLoader.putTemplate(classSimpleName + "." + method.getName() + ".doc.skip", skip);
							}
							String sort = docQuery.sort();
							if (!sort.equals("")) {
								stringLoader.putTemplate(classSimpleName + "." + method.getName() + ".doc.sort", sort);
							}
							String limit = docQuery.limit();
							if (!limit.equals("")) {
								stringLoader.putTemplate(classSimpleName + "." + method.getName() + ".doc.limit",
										limit);
							}
							String[] aggregates = docQuery.aggregate();
							if (aggregates.length > 0) {
								for (int i = 0; i < aggregates.length; i++) {
									String aggregate = aggregates[i];
									stringLoader.putTemplate(
											classSimpleName + "." + method.getName() + ".doc.aggregate." + i, aggregate);
								}
							}
						}
						NeoQuery neoQuery = method.getAnnotation(NeoQuery.class);
						if (null != neoQuery) {
							String value = neoQuery.value();
							if (!value.equals("")) {
								stringLoader.putTemplate(classSimpleName + "." + method.getName() + ".neo.value",
										value);
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("repository scan error.", e);
			}
		}
		cfg.setTemplateLoader(stringLoader);
		DBLogisticsConf.setFreeMakerConf(cfg);
	}
}
