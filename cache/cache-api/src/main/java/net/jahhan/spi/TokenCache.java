package net.jahhan.spi;

import java.util.List;

import net.jahhan.common.extension.annotation.SPI;

@SPI("redis")
public interface TokenCache {
	public void setEx(final byte[] key, final int seconds, final byte[] value);

	public void expire(final String cachedKey, final int seconds);

	public Long ttl(final String key);

	public String get(final String key);

	public byte[] getBinary(final byte[] key);

	public String set(final String key, final String value);

	public String setByte(final byte[] key, final byte[] value);

	public void del(final String... keys);

	public List<String> mget(final String[] keys);

	public List<byte[]> mgetByte(final byte[][] keys);

	public Long pexpireAt(final String cachedKey, final long time);

	public boolean exists(final String key);

	public void setEx(final String key, final int seconds, final String value);
}
