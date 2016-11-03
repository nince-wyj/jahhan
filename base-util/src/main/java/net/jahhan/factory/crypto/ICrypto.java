package net.jahhan.factory.crypto;

/**
 * @author nince
 */
public interface ICrypto {

    public String decrypt(String content, String key);

    public String encrypt(String content, String key);
}
