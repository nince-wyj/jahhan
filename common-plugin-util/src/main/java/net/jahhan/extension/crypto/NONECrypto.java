package net.jahhan.extension.crypto;

import javax.inject.Singleton;

import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.ICrypto;

@Extension("none")
@Singleton
public class NONECrypto implements ICrypto {

	@Override
	public String encrypt(String content, String key) {
		return content;
	}

	@Override
	public String decrypt(String content, String key) {
		return content;
	}
}
