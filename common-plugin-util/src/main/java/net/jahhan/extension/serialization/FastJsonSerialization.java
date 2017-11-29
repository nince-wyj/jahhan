package net.jahhan.extension.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.support.json.FastJsonObjectInput;
import com.alibaba.dubbo.common.serialize.support.json.FastJsonObjectOutput;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.Serialization;

/**
 * FastJsonSerialization
 * 
 * @author william.liangf
 */
@Extension("fastjson")
@Singleton
public class FastJsonSerialization implements Serialization {

	public byte getContentTypeId() {
		return 6;
	}

	public String getContentType() {
		return "text/json";
	}

	public ObjectOutput serialize(OutputStream output) throws IOException {
		return new FastJsonObjectOutput(output);
	}

	public ObjectInput deserialize(InputStream input) throws IOException {
		return new FastJsonObjectInput(input);
	}

}