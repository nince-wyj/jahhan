package net.jahhan.extension.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.OptimizedSerialization;
import com.alibaba.dubbo.common.serialize.support.fst.FstObjectInput;
import com.alibaba.dubbo.common.serialize.support.fst.FstObjectOutput;

import net.jahhan.common.extension.annotation.Extension;

@Extension("fst")
@Singleton
public class FstSerialization implements OptimizedSerialization {

	public byte getContentTypeId() {
		return 9;
	}

	public String getContentType() {
		return "x-application/fst";
	}

	public ObjectOutput serialize(OutputStream out) throws IOException {
		return new FstObjectOutput(out);
	}

	public ObjectInput deserialize(InputStream is) throws IOException {
		return new FstObjectInput(is);
	}
}