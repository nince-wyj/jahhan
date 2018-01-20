package net.jahhan.spi;

import net.jahhan.common.extension.annotation.SPI;

@SPI("redis")
public interface DBSeqCache {
	public long incr(String key);

	public long incrBy(String key, long addValue);

	public void set(String key, long value);
}
