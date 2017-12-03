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
package net.jahhan.extension.statusChecker;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.Constants;
import com.frameworkx.annotation.Activate;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.extension.statusChecker.Status;
import net.jahhan.spi.DataStore;
import net.jahhan.spi.StatusChecker;

/**
 * ThreadPoolStatusChecker
 * 
 * @author william.liangf
 */
@Activate
@Extension("threadpool")
@Singleton
public class ThreadPoolStatusChecker implements StatusChecker {

    public Status check() {
        DataStore dataStore = ExtensionExtendUtil.getExtension(DataStore.class);
        Map<String, Object> executors = dataStore.get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY);

        StringBuilder msg = new StringBuilder();
        Status.Level level = Status.Level.OK;
        for(Map.Entry<String, Object> entry : executors.entrySet()) {
            String port = entry.getKey();
            ExecutorService executor = (ExecutorService) entry.getValue();

            if (executor != null && executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tp = (ThreadPoolExecutor) executor;
                boolean ok = tp.getActiveCount() < tp.getMaximumPoolSize() - 1;
                Status.Level lvl = Status.Level.OK;
                if(!ok) {
                    level = Status.Level.WARN;
                    lvl = Status.Level.WARN;
                }

                if(msg.length() > 0) {
                    msg.append(";");
                }
                msg.append("Pool status:" + lvl
                        + ", max:" + tp.getMaximumPoolSize()
                        + ", core:" + tp.getCorePoolSize()
                        + ", largest:" + tp.getLargestPoolSize()
                        + ", active:" + tp.getActiveCount()
                        + ", task:" + tp.getTaskCount()
                        + ", service port: " + port);
            }
        }
        return msg.length() == 0 ? new Status(Status.Level.UNKNOWN) : new Status(level, msg.toString());
    }

}