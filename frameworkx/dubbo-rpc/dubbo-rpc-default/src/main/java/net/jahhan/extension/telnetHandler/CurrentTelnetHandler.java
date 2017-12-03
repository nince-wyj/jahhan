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
package net.jahhan.extension.telnetHandler;

import javax.inject.Singleton;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.telnet.support.Help;
import com.frameworkx.annotation.Activate;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.TelnetHandler;

/**
 * CurrentServiceTelnetHandler
 * 
 * @author william.liangf
 */
@Activate
@Extension("pwd")
@Singleton
@Help(parameter = "", summary = "Print working default service.", detail = "Print working default service.")
public class CurrentTelnetHandler implements TelnetHandler {
    
    public String telnet(Channel channel, String message) {
        if (message.length() > 0) {
            return "Unsupported parameter " + message + " for pwd.";
        }
        String service = (String) channel.getAttribute(ChangeTelnetHandler.SERVICE_KEY);
        StringBuilder buf = new StringBuilder();
        if (service == null || service.length() == 0) {
            buf.append("/");
        } else {
            buf.append(service);
        }
        return buf.toString();
    }

}