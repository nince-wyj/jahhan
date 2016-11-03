package net.jahhan.handler.serializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.jahhan.handler.SerializerHandler;

public class JavaSerializer implements SerializerHandler {

	@Override
	public byte[] serializeFrom(Object object) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(bos))) {
			oos.writeObject(object);
			oos.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bos.toByteArray();
	}

	@Override
	public Object deserializeInto(byte[] data) {
		BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(data));
		try (ObjectInputStream ois = new ObjectInputStream(bis)) {
			return ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
