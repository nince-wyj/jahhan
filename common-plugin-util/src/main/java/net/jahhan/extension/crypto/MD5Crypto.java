package net.jahhan.extension.crypto;

import java.security.MessageDigest;

import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.exception.JahhanException;
import net.jahhan.spi.common.ICrypto;

/**
 * @author nince
 */
@Slf4j
@Extension("md5")
@Singleton
public class MD5Crypto implements ICrypto {


    @Override
    public String encrypt(String content, String key) {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(content.getBytes("utf8"));
            byte[] md5Result = md.digest();
            result = parseByte2HexStr(md5Result);
        } catch (Exception ex) {
            log.error("encrypt md5加密异常,content:"+content, ex);
            JahhanException.throwException(JahhanErrorCode.ENCRYPT_ERROR, "md5加密异常,content:"+content, ex);
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
        log.error("decrypt md5 无法解密");
        throw new UnsupportedOperationException("decrypt md5 无法解密");
    }
}
