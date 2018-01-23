/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jahhan.extension.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectInput;
import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectOutput;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.Serialization;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

/**
 * RedisProtocol
 * 
 * @author william.liangf
 */
@Extension("redis")
@Singleton
@Slf4j
public class RedisProtocol extends AbstractProtocol {

	public static final int DEFAULT_PORT = 6379;

	public int getDefaultPort() {
		return DEFAULT_PORT;
	}

	public <T> Exporter<T> export(final Invoker<T> invoker) throws JahhanException {
		throw new UnsupportedOperationException("Unsupported export redis service. url: " + invoker.getUrl());
	}

	private Serialization getSerialization(URL url) {
		return ExtensionExtendUtil.getExtension(Serialization.class, url.getParameter(Constants.SERIALIZATION_KEY, "java"));
	}

	public <T> Invoker<T> refer(final Class<T> type, final URL url) throws JahhanException {
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setTestOnBorrow(url.getParameter("test.on.borrow", true));
			config.setTestOnReturn(url.getParameter("test.on.return", false));
			config.setTestWhileIdle(url.getParameter("test.while.idle", false));
			if (url.getParameter("max.idle", 0) > 0)
				config.setMaxIdle(url.getParameter("max.idle", 0));
			if (url.getParameter("min.idle", 0) > 0)
				config.setMinIdle(url.getParameter("min.idle", 0));
			if (url.getParameter("max.total", 0) > 0)
				config.setMaxTotal(url.getParameter("max.total", 0));
			if (url.getParameter("max.wait", 0) > 0)
				config.setMaxWaitMillis(url.getParameter("min.wait", 0));
			if (url.getParameter("num.tests.per.eviction.run", 0) > 0)
				config.setNumTestsPerEvictionRun(url.getParameter("num.tests.per.eviction.run", 0));
			if (url.getParameter("time.between.eviction.runs.millis", 0) > 0)
				config.setTimeBetweenEvictionRunsMillis(url.getParameter("time.between.eviction.runs.millis", 0));
			if (url.getParameter("min.evictable.idle.time.millis", 0) > 0)
				config.setMinEvictableIdleTimeMillis(url.getParameter("min.evictable.idle.time.millis", 0));
			final JedisPool jedisPool = new JedisPool(config, url.getHost(), url.getPort(DEFAULT_PORT),
					url.getParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT));
			final int expiry = url.getParameter("expiry", 0);
			final String get = url.getParameter("get", "get");
			final String set = url.getParameter("set", Map.class.equals(type) ? "put" : "set");
			final String delete = url.getParameter("delete", Map.class.equals(type) ? "remove" : "delete");
			return new AbstractInvoker<T>(type, url) {
				protected Result doInvoke(Invocation invocation) throws Throwable {
					Jedis resource = null;
					try {
						resource = jedisPool.getResource();

						if (get.equals(invocation.getMethodName())) {
							if (invocation.getArguments().length != 1) {
								throw new IllegalArgumentException(
										"The redis get method arguments mismatch, must only one arguments. interface: "
												+ type.getName() + ", method: " + invocation.getMethodName() + ", url: "
												+ url);
							}
							byte[] value = resource.get(String.valueOf(invocation.getArguments()[0]).getBytes());
							if (value == null) {
								return new RpcResult();
							}
							ObjectInput oin = getSerialization(url).deserialize(url, new ByteArrayInputStream(value));
							return new RpcResult(oin.readObject());
						} else if (set.equals(invocation.getMethodName())) {
							if (invocation.getArguments().length != 2) {
								throw new IllegalArgumentException(
										"The redis set method arguments mismatch, must be two arguments. interface: "
												+ type.getName() + ", method: " + invocation.getMethodName() + ", url: "
												+ url);
							}
							byte[] key = String.valueOf(invocation.getArguments()[0]).getBytes();
							ByteArrayOutputStream output = new ByteArrayOutputStream();
							ObjectOutput value = getSerialization(url).serialize(url, output);
							value.writeObject(invocation.getArguments()[1]);
							resource.set(key, output.toByteArray());
							if (expiry > 1000) {
								resource.expire(key, expiry / 1000);
							}
							return new RpcResult();
						} else if (delete.equals(invocation.getMethodName())) {
							if (invocation.getArguments().length != 1) {
								throw new IllegalArgumentException(
										"The redis delete method arguments mismatch, must only one arguments. interface: "
												+ type.getName() + ", method: " + invocation.getMethodName() + ", url: "
												+ url);
							}
							resource.del(String.valueOf(invocation.getArguments()[0]).getBytes());
							return new RpcResult();
						} else {
							throw new UnsupportedOperationException(
									"Unsupported method " + invocation.getMethodName() + " in redis service.");
						}
					} catch (Throwable t) {
						JahhanException re = new JahhanException(
								"Failed to invoke redis service method. interface: " + type.getName() + ", method: "
										+ invocation.getMethodName() + ", url: " + url + ", cause: " + t.getMessage(),
								t);
						if (t instanceof TimeoutException || t instanceof SocketTimeoutException) {
							re.setCode(JahhanErrorCode.TIMEOUT_EXCEPTION);
						} else if (t instanceof JedisConnectionException || t instanceof IOException) {
							re.setCode(JahhanErrorCode.NETWORK_EXCEPTION);
						} else if (t instanceof JedisDataException) {
							re.setCode(JahhanErrorCode.SERIALIZATION_EXCEPTION);
						}
						throw re;
					} finally {
						if (resource != null) {
							try {
								jedisPool.returnResource(resource);
							} catch (Throwable t) {
								log.warn("returnResource error: " + t.getMessage(), t);
							}
						}
					}
				}

				public void destroy() {
					super.destroy();
					try {
						jedisPool.destroy();
					} catch (Throwable e) {
						log.warn(e.getMessage(), e);
					}
				}
			};
		} catch (Throwable t) {
			throw new JahhanException("Failed to refer redis service. interface: " + type.getName() + ", url: "
					+ url + ", cause: " + t.getMessage(), t);
		}
	}

}
