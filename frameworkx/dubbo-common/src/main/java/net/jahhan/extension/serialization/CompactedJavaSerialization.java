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
package net.jahhan.extension.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.URL;
import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectInput;
import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.support.java.JavaObjectInput;
import com.alibaba.dubbo.common.serialize.support.java.JavaObjectOutput;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.Serialization;

/**
 * @author ding.lid
 */
@Extension("compactedjava")
@Singleton
public class CompactedJavaSerialization implements Serialization {

    public byte getContentTypeId() {
        return 4;
    }

    public String getContentType() {
        return "x-application/compactedjava";
    }

    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        return new JavaObjectOutput(out, true);
    }

    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        return new JavaObjectInput(is, true);
    }

}