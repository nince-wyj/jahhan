package net.jahhan.cache.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectInput;
import net.jahhan.com.alibaba.dubbo.common.serialize.ObjectOutput;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.common.extension.utils.LogUtil;
import net.jahhan.spi.common.Serialization;

public class SerializerUtil {
	private static Serialization serializer = BaseContext.CTX.getInjector().getInstance(Serialization.class);

	public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
		T readObject = null;
		try {
			ObjectInput deserialize = serializer.deserialize(new ByteArrayInputStream(bytes));
			readObject = deserialize.readObject(clazz);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
		return readObject;
	}

	public static byte[] serializeFrom(Object object) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutput objectOutput;
		try {
			objectOutput = serializer.serialize(byteArrayOutputStream);
			objectOutput.writeObject(object);
			objectOutput.flushBuffer();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			LogUtil.error(e.getMessage(), e);
		}
		return null;
	}
}
