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
package net.jahhan.extension.filter;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.validation.Validator;
import com.frameworkx.annotation.Activate;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.spi.Filter;
import net.jahhan.spi.Validation;

/**
 * ValidationFilter
 * 
 * @author william.liangf
 */
@Activate(group = { Constants.CONSUMER, Constants.PROVIDER },  order = 10000)
@Extension("validation")
@Singleton
public class ValidationFilter implements Filter {

	@Inject
    private Validation validation;

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws JahhanException {
        if (validation != null && ! invocation.getMethodName().startsWith("$")) {
            try {
                Validator validator = validation.getValidator(invoker.getUrl());
                if (validator != null) {
                    validator.validate(invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments());
                }
            } catch (JahhanException e) {
                throw e;
            } catch (Throwable t) {
                throw new JahhanException(t.getMessage(), t);
            }
        }
        return invoker.invoke(invocation);
    }

}
