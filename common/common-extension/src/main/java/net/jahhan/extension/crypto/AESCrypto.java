package net.jahhan.extension.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.exception.JahhanException;
import net.jahhan.common.extension.utils.Base64Util;
import net.jahhan.spi.common.ICrypto;

/**
 * @author nince
 */
@Slf4j
@Extension("aes")
@Singleton
public class AESCrypto implements ICrypto {

	@Override
	public String encrypt(String content, String key) {
		try {
			byte[] contentBytes = content.getBytes("UTF-8");
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encryptResult = cipher.doFinal(contentBytes);
			String result = Base64Util.encode(encryptResult);
			return result = result.replace("\n", "").replace("\r", "");
		} catch (Exception e) {
			log.error("加密异常：context:" + content + ",key:" + key, e);
			JahhanException.throwException(JahhanErrorCode.ENCRYPT_ERROR, "加密异常：context:" + content + ",key:" + key,
					e);
		}
		return null;
	}

	// "AES/CBC/NoPadding"
	public String encrypt(String content, String key, String mode) {
		try {
			byte[] contentBytes = content.getBytes("UTF-8");
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance(mode);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encryptResult = cipher.doFinal(contentBytes);
			String result = Base64Util.encode(encryptResult);
			return result = result.replace("\n", "").replace("\r", "");
		} catch (Exception e) {
			log.error("加密异常：context:" + content + ",key:" + key, e);
			JahhanException.throwException(JahhanErrorCode.ENCRYPT_ERROR, "加密异常：context:" + content + ",key:" + key,
					e);
		}
		return null;
	}

	@Override
	public String decrypt(String content, String key) {
		String result = null;
		byte[] decryptResult = null;
		try {
			byte[] contentBytes = Base64Util.decode(content);
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			decryptResult = cipher.doFinal(contentBytes);
			if (decryptResult != null) {
				result = new String(decryptResult, "UTF-8");
			}
		} catch (Exception e) {
			log.error("解密异常：context:" + content + ",key:" + key, e);
			JahhanException.throwException(JahhanErrorCode.DECRYPT_ERROR, "解密异常：context:" + content + ",key:" + key,
					e);
		}
		return result;

	}

	public String decrypt(String content, String key, String mode) {
		String result = null;
		byte[] decryptResult = null;
		try {
			byte[] contentBytes = Base64Util.decode(content);
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance(mode);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			decryptResult = cipher.doFinal(contentBytes);
			if (decryptResult != null) {
				result = new String(decryptResult, "UTF-8");
			}
		} catch (Exception e) {
			log.error("解密异常：context:" + content + ",key:" + key, e);
			JahhanException.throwException(JahhanErrorCode.DECRYPT_ERROR, "解密异常：context:" + content + ",key:" + key,
					e);
		}
		return result;
	}
}
