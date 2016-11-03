package net.jahhan.handler.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Singleton;

import com.alibaba.com.caucho.hessian.io.HessianInput;
import com.alibaba.com.caucho.hessian.io.HessianOutput;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;

import net.jahhan.handler.SerializerHandler;

@Singleton
public class HessianSerializer implements SerializerHandler {
	private SerializerFactory factory = new SerializerFactory();

	@Override
	public byte[] serializeFrom(Object object) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		HessianOutput ho = new HessianOutput(os);
		try {
			ho.writeObject(object);
		} catch (IOException e) {

		}
		return os.toByteArray();
	}

	@Override
	public Object deserializeInto(byte[] data) {
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		HessianInput hi = new HessianInput(is);
		hi.setSerializerFactory(factory);
		try {
			return hi.readObject();
		} catch (Exception e) {

		}
		return null;
	}

}
