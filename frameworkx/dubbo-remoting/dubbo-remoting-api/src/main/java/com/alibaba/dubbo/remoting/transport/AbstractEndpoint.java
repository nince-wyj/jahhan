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
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.Resetable;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.transport.codec.CodecAdapter;
import com.frameworkx.common.extension.utils.ExtensionExtendUtil;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.spi.ChannelHandler;
import net.jahhan.spi.Codec;
import net.jahhan.spi.Codec2;

/**
 * AbstractEndpoint
 * 
 * @author william.liangf
 */
@Slf4j
public abstract class AbstractEndpoint extends AbstractPeer implements Resetable {

	private Codec2 codec;

	private int timeout;

	private int connectTimeout;

	public AbstractEndpoint(URL url, ChannelHandler handler) {
		super(url, handler);
		this.codec = getChannelCodec(url);
		this.timeout = url.getPositiveParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
		this.connectTimeout = url.getPositiveParameter(Constants.CONNECT_TIMEOUT_KEY,
				Constants.DEFAULT_CONNECT_TIMEOUT);
	}

	public void reset(URL url) {
		if (isClosed()) {
			throw new IllegalStateException(
					"Failed to reset parameters " + url + ", cause: Channel closed. channel: " + getLocalAddress());
		}
		try {
			if (url.hasParameter(Constants.TIMEOUT_KEY)) {
				int t = url.getParameter(Constants.TIMEOUT_KEY, 0);
				if (t > 0) {
					this.timeout = t;
				}
			}
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		try {
			if (url.hasParameter(Constants.CONNECT_TIMEOUT_KEY)) {
				int t = url.getParameter(Constants.CONNECT_TIMEOUT_KEY, 0);
				if (t > 0) {
					this.connectTimeout = t;
				}
			}
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
		try {
			if (url.hasParameter(Constants.CODEC_KEY)) {
				this.codec = getChannelCodec(url);
			}
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}

	@Deprecated
	public void reset(com.alibaba.dubbo.common.Parameters parameters) {
		reset(getUrl().addParameters(parameters.getParameters()));
	}

	protected Codec2 getCodec() {
		return codec;
	}

	protected int getTimeout() {
		return timeout;
	}

	protected int getConnectTimeout() {
		return connectTimeout;
	}

	protected static Codec2 getChannelCodec(URL url) {
		String codecName = url.getParameter(Constants.CODEC_KEY, "telnet");
		if (ExtensionExtendUtil.hasExtension(Codec2.class, codecName)) {
			return ExtensionExtendUtil.getExtension(Codec2.class, codecName);
		} else {
			return new CodecAdapter(ExtensionExtendUtil.getExtension(Codec.class, codecName));
		}
	}

}