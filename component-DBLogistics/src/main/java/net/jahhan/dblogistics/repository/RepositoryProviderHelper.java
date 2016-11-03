package net.jahhan.dblogistics.repository;

import java.util.HashMap;
import java.util.Map;

import net.jahhan.dblogistics.entity.SuperEntity;
import net.jahhan.dblogistics.repository.query.SuperRepositoryImpl;

public class RepositoryProviderHelper {

	private RepositoryProviderHelper() {

	}

	private static RepositoryProviderHelper instance = new RepositoryProviderHelper();

	public static RepositoryProviderHelper getInstance() {
		return instance;
	}

	private Map<Class<? extends SuperEntity>, SuperRepositoryImpl<? extends SuperEntity>> repositories = new HashMap<>();

	public <T extends SuperEntity> void regist(Class<T> clazz) {
		SuperRepositoryImpl<T> repository = new SuperRepositoryImpl<T>(clazz);
		repositories.put(clazz, repository);
	}

	public SuperRepositoryImpl<?> get(Class<?> clazz) {
		return (SuperRepositoryImpl<?>) repositories.get(clazz);
	}
}
