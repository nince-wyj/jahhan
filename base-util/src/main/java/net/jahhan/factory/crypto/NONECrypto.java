package net.jahhan.factory.crypto;

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
