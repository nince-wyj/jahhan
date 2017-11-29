package net.jahhan.extension.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.OptimizedSerialization;
import com.alibaba.dubbo.common.serialize.support.kryo.KryoObjectInput;
import com.alibaba.dubbo.common.serialize.support.kryo.KryoObjectOutput;

import net.jahhan.common.extension.annotation.Extension;

/**
 * TODO for now kryo serialization doesn't deny classes that don't implement the
 * serializable interface
 *
 * @author lishen
 */
@Extension("kryo")
@Singleton
public class KryoSerialization implements OptimizedSerialization {

	public byte getContentTypeId() {
		return 8;
	}

	public String getContentType() {
		return "x-application/kryo";
	}

	public ObjectOutput serialize(OutputStream out) throws IOException {
		return new KryoObjectOutput(out);
	}

	public ObjectInput deserialize(InputStream is) throws IOException {
		return new KryoObjectInput(is);
	}
}