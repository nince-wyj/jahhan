package net.jahhan.spi;

import net.jahhan.annotation.SPI;

@SPI("java")
public interface SerializerHandler {

	public byte[] serializeFrom(Object object);

	public Object deserializeInto(byte[] data);
}
