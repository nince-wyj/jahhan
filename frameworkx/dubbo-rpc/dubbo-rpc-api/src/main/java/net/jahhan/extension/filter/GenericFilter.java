/*
 * Copyright 1999-2011 Alibaba Group.
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
package net.jahhan.extension.filter;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.beanutil.JavaBeanAccessor;
import com.alibaba.dubbo.common.beanutil.JavaBeanDescriptor;
import com.alibaba.dubbo.common.beanutil.JavaBeanSerializeUtil;
import com.alibaba.dubbo.common.io.UnsafeByteArrayInputStream;
import com.alibaba.dubbo.common.io.UnsafeByteArrayOutputStream;
import com.alibaba.dubbo.common.utils.PojoUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import com.frameworkx.annotation.Activate;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.utils.StringUtils;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.Filter;
import net.jahhan.spi.Serialization;

/**
 * GenericInvokerFilter.
 * 
 * @author william.liangf
 */
@Activate(group = Constants.PROVIDER, order = -20000)
@Extension("generic")
@Singleton
public class GenericFilter implements Filter {

	public Result invoke(Invoker<?> invoker, Invocation inv) throws JahhanException {
		if (inv.getMethodName().equals(Constants.$INVOKE) && inv.getArguments() != null
				&& inv.getArguments().length == 3
				&& !ProtocolUtils.isGeneric(invoker.getUrl().getParameter(Constants.GENERIC_KEY))) {
			String name = ((String) inv.getArguments()[0]).trim();
			String[] types = (String[]) inv.getArguments()[1];
			Object[] args = (Object[]) inv.getArguments()[2];
			try {
				Method method = ReflectUtils.findMethodByMethodSignature(invoker.getInterface(), name, types);
				Class<?>[] params = method.getParameterTypes();
				if (args == null) {
					args = new Object[params.length];
				}
				String generic = inv.getAttachment(Constants.GENERIC_KEY);
				if (StringUtils.isEmpty(generic) || ProtocolUtils.isDefaultGenericSerialization(generic)) {
					args = PojoUtils.realize(args, params, method.getGenericParameterTypes());
				} else if (ProtocolUtils.isJavaGenericSerialization(generic)) {
					for (int i = 0; i < args.length; i++) {
						if (byte[].class == args[i].getClass()) {
							try {
								UnsafeByteArrayInputStream is = new UnsafeByteArrayInputStream((byte[]) args[i]);
								args[i] = ExtensionExtendUtil
										.getExtension(Serialization.class, Constants.GENERIC_SERIALIZATION_NATIVE_JAVA)
										.deserialize(null, is).readObject();
							} catch (Exception e) {
								throw new JahhanException("Deserialize argument [" + (i + 1) + "] failed.", e);
							}
						} else {
							throw new JahhanException(new StringBuilder(32).append("Generic serialization [")
									.append(Constants.GENERIC_SERIALIZATION_NATIVE_JAVA)
									.append("] only support message type ").append(byte[].class)
									.append(" and your message type is ").append(args[i].getClass()).toString());
						}
					}
				} else if (ProtocolUtils.isBeanGenericSerialization(generic)) {
					for (int i = 0; i < args.length; i++) {
						if (args[i] instanceof JavaBeanDescriptor) {
							args[i] = JavaBeanSerializeUtil.deserialize((JavaBeanDescriptor) args[i]);
						} else {
							throw new JahhanException(new StringBuilder(32).append("Generic serialization [")
									.append(Constants.GENERIC_SERIALIZATION_BEAN).append("] only support message type ")
									.append(JavaBeanDescriptor.class.getName()).append(" and your message type is ")
									.append(args[i].getClass().getName()).toString());
						}
					}
				}
				Result result = invoker.invoke(new RpcInvocation(method, args, inv.getAttachments()));
				if (result.hasException() && !(result.getException() instanceof GenericException)) {
					return new RpcResult(new GenericException(result.getException()));
				}
				if (ProtocolUtils.isJavaGenericSerialization(generic)) {
					try {
						UnsafeByteArrayOutputStream os = new UnsafeByteArrayOutputStream(512);
						ExtensionExtendUtil.getExtension(Serialization.class, Constants.GENERIC_SERIALIZATION_NATIVE_JAVA)
								.serialize(null, os).writeObject(result.getValue());
						return new RpcResult(os.toByteArray());
					} catch (IOException e) {
						throw new JahhanException("Serialize result failed.", e);
					}
				} else if (ProtocolUtils.isBeanGenericSerialization(generic)) {
					return new RpcResult(JavaBeanSerializeUtil.serialize(result.getValue(), JavaBeanAccessor.METHOD));
				} else {
					return new RpcResult(PojoUtils.generalize(result.getValue()));
				}
			} catch (NoSuchMethodException e) {
				throw new JahhanException(e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				throw new JahhanException(e.getMessage(), e);
			}
		}
		return invoker.invoke(inv);
	}

}