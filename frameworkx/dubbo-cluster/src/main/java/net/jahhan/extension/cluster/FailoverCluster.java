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
package net.jahhan.extension.cluster;

import javax.inject.Singleton;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.support.FailoverClusterInvoker;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.spi.Cluster;

/**
 * 失败转移，当出现失败，重试其它服务器，通常用于读操作，但重试会带来更长延迟。 
 * 
 * <a href="http://en.wikipedia.org/wiki/Failover">Failover</a>
 * 
 * @author william.liangf
 */
@Extension("failover")
@Singleton
public class FailoverCluster implements Cluster {

    public final static String NAME = "failover";

    public <T> Invoker<T> join(Directory<T> directory) throws JahhanException {
        return new FailoverClusterInvoker<T>(directory);
    }

}