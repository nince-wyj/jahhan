package net.jahhan.factory.crypto;

import java.security.MessageDigest;

import org.slf4j.Logger;

import net.jahhan.factory.LoggerFactory;

/**
 * @author nince
 */
public class MD5Crypto implements ICrypto {

    private final Logger logger = LoggerFactory.getInstance().getLogger(AESCrypto.class);

    @Override
    public String encrypt(String content, String key) {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(content.getBytes("utf8"));
            byte[] md5Result = md.digest();
            result = parseByte2HexStr(md5Result);
        } catch (Exception ex) {
            logger.error("encrypt md5加密异常", ex);
        }
        return result;
    }

    private String parseByte2HexStr(byte buf[]) {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    @Override
    public String decrypt(String content, String key) {
        logger.error("decrypt md5 无法解密");
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
