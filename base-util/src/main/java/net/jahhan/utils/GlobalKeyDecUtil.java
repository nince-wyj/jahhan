package net.jahhan.utils;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.enumeration.CryptEnum;
import net.jahhan.context.ApplicationContext;
import net.jahhan.factory.crypto.ICrypto;

/**
 * @author nince
 */
public class GlobalKeyDecUtil {
	private GlobalKeyDecUtil() {
	}

	private static ICrypto iCrypto = ApplicationContext.CTX.getCrypto(CryptEnum.AES);

	public static String decryption(String ciphertext) {
		return iCrypto.decrypt(ciphertext, SysConfiguration.getEncryptkey());
	}
}
