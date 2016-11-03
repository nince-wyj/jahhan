package net.jahhan.init;

import java.lang.reflect.Proxy;

import com.google.inject.Provider;

import net.jahhan.dblogistics.SuperRepository;
import net.jahhan.dblogistics.entity.SuperEntity;
import net.jahhan.dblogistics.repository.query.QueryProxy;

public class RepositoryPovider<T extends SuperEntity,S extends SuperRepository<T>> implements
		Provider<SuperRepository<T>> {
	private Class<S> clazz;
	private Class<T> ts;

	public RepositoryPovider(Class<S> clazz,Class<T> ts) {
		super();
		this.clazz = clazz;
		this.ts=ts;
	}

	@SuppressWarnings("unchecked")
	@Override
	public S get() {
		return (S) Proxy.newProxyInstance(clazz.getClassLoader(),
				new Class[] { clazz }, new QueryProxy(ts));
	}
}
