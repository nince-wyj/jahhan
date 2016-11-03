package net.jahhan.factory.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.LoggerFactory;
import net.jahhan.utils.Base64Util;

/**
 * @author nince
 */
public class AESCrypto implements ICrypto {

	private final Logger logger = LoggerFactory.getInstance().getLogger(
			AESCrypto.class);

	@Override
	public String encrypt(String content, String key) {
		try {
			byte[] contentBytes = content.getBytes("UTF-8");
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"),
					"AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encryptResult = cipher.doFinal(contentBytes);
			String result = Base64Util.encode(encryptResult);
			return result = result.replace("\n", "").replace("\r", "");
		} catch (Exception e) {
			logger.error("加密异常：context:" + content + ",key:" + key, e);
			FrameworkException.throwException(SystemErrorCode.ENCRYPT_ERROR,
					"加密异常");
		}
		return null;
	}

	// "AES/CBC/NoPadding"
	public String encrypt(String content, String key, String mode) {
		try {
			byte[] contentBytes = content.getBytes("UTF-8");
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"),
					"AES");
			Cipher cipher = Cipher.getInstance(mode);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encryptResult = cipher.doFinal(contentBytes);
			String result = Base64Util.encode(encryptResult);
			return result = result.replace("\n", "").replace("\r", "");
		} catch (Exception e) {
			logger.error("加密异常：context:" + content + ",key:" + key, e);
			FrameworkException.throwException(SystemErrorCode.ENCRYPT_ERROR,
					"加密异常");
		}
		return null;
	}

	@Override
	public String decrypt(String content, String key) {
		String result = null;
		byte[] decryptResult = null;
		try {
			byte[] contentBytes = Base64Util.decode(content);
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"),
					"AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			decryptResult = cipher.doFinal(contentBytes);
			if (decryptResult != null) {
				result = new String(decryptResult, "UTF-8");
			}
		} catch (Exception e) {
			logger.error("解密异常：context:" + content + ",key:" + key, e);
			FrameworkException.throwException(SystemErrorCode.DECRYPT_ERROR,
					"解密异常");
		}
		return result;

	}

	public String decrypt(String content, String key, String mode) {
		String result = null;
		byte[] decryptResult = null;
		try {
			byte[] contentBytes = Base64Util.decode(content);
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"),
					"AES");
			Cipher cipher = Cipher.getInstance(mode);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			decryptResult = cipher.doFinal(contentBytes);
			if (decryptResult != null) {
				result = new String(decryptResult, "UTF-8");
			}
		} catch (Exception e) {
			logger.error("解密异常：context:" + content + ",key:" + key, e);
			FrameworkException.throwException(SystemErrorCode.DECRYPT_ERROR,
					"解密异常");
		}
		return result;

	}
}
