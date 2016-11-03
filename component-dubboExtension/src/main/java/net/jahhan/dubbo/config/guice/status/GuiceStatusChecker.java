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
package net.jahhan.dubbo.config.guice.status;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.status.Status;
import com.alibaba.dubbo.common.status.StatusChecker;
import com.google.inject.Injector;

import net.jahhan.context.ApplicationContext;

@Activate
public class GuiceStatusChecker implements StatusChecker {

	protected static final Logger logger = LoggerFactory.getLogger(GuiceStatusChecker.class);

	public Status check() {
		Injector Injector = ApplicationContext.CTX.getInjector();
		if (Injector == null) {
			return new Status(Status.Level.UNKNOWN);
		}
		Status.Level level = Status.Level.OK;
		StringBuilder buf = new StringBuilder();
		return new Status(level, buf.toString());
	}

}