package net.jahhan.utils;

import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.context.BaseContext;
import net.jahhan.factory.crypto.ICrypto;

public class SecrityUtil {

	public static boolean isEqual(String password, String salt, String encryptPassword) {
		ICrypto iCrypto = BaseContext.CTX.getCrypto(CryptEnum.MD5);
		if (null == salt)
			return encryptPassword.equals(iCrypto.encrypt(password, ""));
		return encryptPassword.equals(iCrypto.encrypt(password + ":" + salt, ""));
	}

	public static String encrypt(String password, String salt) {
		ICrypto iCrypto = BaseContext.CTX.getCrypto(CryptEnum.MD5);
		if (null == salt)
			return iCrypto.encrypt(password, "");
		return iCrypto.encrypt(password + ":" + salt, "");
	}
}
