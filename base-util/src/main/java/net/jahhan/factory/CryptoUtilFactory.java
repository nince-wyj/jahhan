package net.jahhan.factory;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.factory.crypto.AESCrypto;
import net.jahhan.factory.crypto.ICrypto;
import net.jahhan.factory.crypto.MD5Crypto;
import net.jahhan.factory.crypto.NONECrypto;

/**
 * 加解密工厂类
 * 
 */
@Singleton
public class CryptoUtilFactory {

	private final Map<CryptEnum, ICrypto> cryptoMap = new HashMap<CryptEnum, ICrypto>(5, 1);

	public CryptoUtilFactory() {
		cryptoMap.put(CryptEnum.AES, new AESCrypto());
		cryptoMap.put(CryptEnum.MD5, new MD5Crypto());
		cryptoMap.put(CryptEnum.SIGN, new MD5Crypto());
		cryptoMap.put(CryptEnum.PLAIN, new NONECrypto());
		cryptoMap.put(CryptEnum.LOGIN, new AESCrypto());
	}

	public ICrypto getCrypto(CryptEnum cryptEnum) {
		return cryptoMap.get(cryptEnum);
	}
}
