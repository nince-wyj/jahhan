/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.guice.annotation;

import java.beans.PropertyDescriptor;

import javax.inject.Provider;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

/**
 * Convenience class used to map a Guice {@link Provider} to a Spring bean.
 * 
 * @author Dave Syer
 */
class GuiceFactoryBean<T> implements FactoryBean<T>, InstantiationAwareBeanPostProcessor {
	private final Provider<T> provider;
	private final Class<T> beanType;

	public GuiceFactoryBean(Class<T> beanType, Provider<T> provider) {
		this.provider = provider;
		this.beanType = beanType;
	}

	@Override
	public T getObject() throws Exception {
		return (T) provider.get();
	}

	@Override
	public Class<?> getObjectType() {
		return beanType;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean,
			String beanName) throws BeansException {
		return pvs;
	}
}