package net.jahhan.extension.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Singleton;

import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectInput;
import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectOutput;
import net.jahhan.com.alibaba.dubbo.common.serialize.support.dubbo.GenericObjectInput;
import net.jahhan.com.alibaba.dubbo.common.serialize.support.dubbo.GenericObjectOutput;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.common.Serialization;

/**
 * @author ding.lid
 */
@Extension("dubbo")
@Singleton
public class DubboSerialization implements Serialization {

    public byte getContentTypeId() {
        return 1;
    }

    public String getContentType() {
        return "x-application/dubbo";
    }

    public ObjectOutput serialize( OutputStream out) throws IOException {
        return new GenericObjectOutput(out);
    }

    public ObjectInput deserialize( InputStream is) throws IOException {
        return new GenericObjectInput(is);
    }

}