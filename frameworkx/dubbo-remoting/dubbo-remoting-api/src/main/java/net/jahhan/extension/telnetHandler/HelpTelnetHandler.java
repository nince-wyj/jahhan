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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.telnet.support.Help;
import com.alibaba.dubbo.remoting.telnet.support.TelnetUtils;
import com.frameworkx.annotation.Activate;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.TelnetHandler;

/**
 * HelpTelnetHandler
 * 
 * @author william.liangf
 */
@Activate
@Help(parameter = "[command]", summary = "Show help.", detail = "Show help.")
@Extension("help")
@Singleton
public class HelpTelnetHandler implements TelnetHandler {

	public String telnet(Channel channel, String message) {
		if (message.length() > 0) {
			if (!ExtensionExtendUtil.hasExtension(TelnetHandler.class, message)) {
				return "No such command " + message;
			}
			TelnetHandler handler = ExtensionExtendUtil.getExtension(TelnetHandler.class, message);
			Help help = handler.getClass().getAnnotation(Help.class);
			StringBuilder buf = new StringBuilder();
			buf.append("Command:\r\n    ");
			buf.append(message + " " + help.parameter().replace("\r\n", " ").replace("\n", " "));
			buf.append("\r\nSummary:\r\n    ");
			buf.append(help.summary().replace("\r\n", " ").replace("\n", " "));
			buf.append("\r\nDetail:\r\n    ");
			buf.append(help.detail().replace("\r\n", "    \r\n").replace("\n", "    \n"));
			return buf.toString();
		} else {
			List<List<String>> table = new ArrayList<List<String>>();
			List<TelnetHandler> handlers = ExtensionExtendUtil.getActivateExtension(TelnetHandler.class, channel.getUrl(),
					"telnet");
			if (handlers != null && handlers.size() > 0) {
				for (TelnetHandler handler : handlers) {
					Help help = handler.getClass().getAnnotation(Help.class);
					List<String> row = new ArrayList<String>();
					String parameter = " " + ExtensionExtendUtil.getExtensionName(TelnetHandler.class, handler.getClass())
							+ " " + (help != null ? help.parameter().replace("\r\n", " ").replace("\n", " ") : "");
					row.add(parameter.length() > 50 ? parameter.substring(0, 50) + "..." : parameter);
					String summary = help != null ? help.summary().replace("\r\n", " ").replace("\n", " ") : "";
					row.add(summary.length() > 50 ? summary.substring(0, 50) + "..." : summary);
					table.add(row);
				}
			}
			return "Please input \"help [command]\" show detail.\r\n" + TelnetUtils.toList(table);
		}
	}

}