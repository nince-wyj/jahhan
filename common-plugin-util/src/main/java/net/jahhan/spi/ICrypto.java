package net.jahhan.spi;

import net.jahhan.common.extension.annotation.SPI;

/**
 * @author nince
 */
@SPI("none")
public interface ICrypto {

	public String decrypt(String content, String key);

	public String encrypt(String content, String key);
}
