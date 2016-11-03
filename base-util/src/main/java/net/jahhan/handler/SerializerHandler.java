package net.jahhan.handler;

public interface SerializerHandler {

	public byte[] serializeFrom(Object object);

	public Object deserializeInto(byte[] data);
}
