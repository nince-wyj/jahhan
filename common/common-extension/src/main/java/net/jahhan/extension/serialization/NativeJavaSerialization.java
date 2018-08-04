package net.jahhan.extension.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Singleton;

import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectInput;
import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectOutput;
import net.jahhan.com.alibaba.dubbo.common.serialize.support.nativejava.NativeJavaObjectInput;
import net.jahhan.com.alibaba.dubbo.common.serialize.support.nativejava.NativeJavaObjectOutput;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.common.Serialization;

/**
 * @author <a href="mailto:gang.lvg@alibaba-inc.com">kimi</a>
 */
@Extension("nativejava")
@Singleton
public class NativeJavaSerialization implements Serialization {

	public static final String NAME = "nativejava";

	public byte getContentTypeId() {
		return 7;
	}

	public String getContentType() {
		return "x-application/nativejava";
	}

	public ObjectOutput serialize(OutputStream output) throws IOException {
		return new NativeJavaObjectOutput(output);
	}

	public ObjectInput deserialize(InputStream input) throws IOException {
		return new NativeJavaObjectInput(input);
	}
}
