package net.jahhan.extension.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.support.java.JavaObjectInput;
import com.alibaba.dubbo.common.serialize.support.java.JavaObjectOutput;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.common.Serialization;

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

    public ObjectOutput serialize( OutputStream out) throws IOException {
        return new JavaObjectOutput(out, true);
    }

    public ObjectInput deserialize( InputStream is) throws IOException {
        return new JavaObjectInput(is, true);
    }

}