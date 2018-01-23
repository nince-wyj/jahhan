/*
 * Copyright 1999-2011 Alibaba Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.dubbo.rpc.protocol.dubbo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Decodeable;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.transport.CodecSupport;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.support.RpcUtils;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.com.alibaba.dubbo.common.serialize.Cleanable;
import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectInput;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.common.extension.utils.StringUtils;
import net.jahhan.spi.Codec;

/**
 * @author <a href="mailto:gang.lvg@alibaba-inc.com">kimi</a>
 */
@Slf4j
public class DecodeableRpcResult extends RpcResult implements Codec, Decodeable {

    private Channel     channel;

    private byte        serializationType;

    private InputStream inputStream;

    private Response    response;

    private Invocation  invocation;

    private volatile boolean hasDecoded;

    public DecodeableRpcResult(Channel channel, Response response, InputStream is, Invocation invocation, byte id) {
        Assert.notNull(channel, "channel == null",JahhanErrorCode.UNKNOW_ERROR);
        Assert.notNull(response, "response == null",JahhanErrorCode.UNKNOW_ERROR);
        Assert.notNull(is, "inputStream == null",JahhanErrorCode.UNKNOW_ERROR);
        this.channel = channel;
        this.response = response;
        this.inputStream = is;
        this.invocation = invocation;
        this.serializationType = id;
    }

    public void encode(Channel channel, OutputStream output, Object message) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Object decode(Channel channel, InputStream input) throws IOException {
        ObjectInput in = CodecSupport.getSerialization(channel.getUrl(), serializationType)
            .deserialize(channel.getUrl(), input);

        try {
            byte flag = in.readByte();
            switch (flag) {
                case DubboCodec.RESPONSE_NULL_VALUE:
                    break;
                case DubboCodec.RESPONSE_VALUE:
                    try {
                        Type[] returnType = RpcUtils.getReturnTypes(invocation);
                        setValue(returnType == null || returnType.length == 0 ? in.readObject() :
                                     (returnType.length == 1 ? in.readObject((Class<?>) returnType[0])
                                         : in.readObject((Class<?>) returnType[0], returnType[1])));
                    } catch (ClassNotFoundException e) {
                        throw new IOException(StringUtils.toString("Read response data failed.", e));
                    }
                    break;
                case DubboCodec.RESPONSE_WITH_EXCEPTION:
                    try {
                        Object obj = in.readObject();
                        if (obj instanceof Throwable == false)
                            throw new IOException("Response data error, expect Throwable, but get " + obj);
                        setException((Throwable) obj);
                    } catch (ClassNotFoundException e) {
                        throw new IOException(StringUtils.toString("Read response data failed.", e));
                    }
                    break;
                default:
                    throw new IOException("Unknown result flag, expect '0' '1' '2', get " + flag);
            }
            return this;
        } finally {
            // modified by lishen
            if (in instanceof Cleanable) {
                ((Cleanable) in).cleanup();
            }
        }
    }

    public void decode() throws Exception {
        if (!hasDecoded && channel != null && inputStream != null) {
            try {
                decode(channel, inputStream);
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("Decode rpc result failed: " + e.getMessage(), e);
                }
                response.setStatus(Response.CLIENT_ERROR);
                response.setErrorMessage(StringUtils.toString(e));
            } finally {
                hasDecoded = true;
            }
        }
    }

}
