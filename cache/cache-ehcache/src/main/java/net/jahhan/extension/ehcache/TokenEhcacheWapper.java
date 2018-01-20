package net.jahhan.extension.ehcache;

import java.util.ArrayList;
import java.util.List;

import net.jahhan.cache.ehcache.EhcacheFactory;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.spi.TokenCache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Extension("ehcache")
public class TokenEhcacheWapper implements TokenCache {
	private CacheManager cacheManager = EhcacheFactory.getCacheManager();

	@Override
	public void setEx(byte[] key, int seconds, byte[] value) {
		String strKey = new String(key);
		String[] keySplit = strKey.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = new Element(keySplit[2], new String(value));
		cache.put(element);
	}

	@Override
	public void expire(String cachedKey, int seconds) {
		String[] keySplit = cachedKey.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		cache.get(keySplit[2]);
	}

	@Override
	public Long ttl(final String key) {
		String[] keySplit = key.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = cache.get(keySplit[2]);
		return element.getExpirationTime();
	}

	@Override
	public String get(String key) {
		String[] keySplit = key.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = cache.get(keySplit[2]);
		return (String) element.getObjectValue();
	}

	@Override
	public byte[] getBinary(byte[] key) {
		String strKey = new String(key);
		String[] keySplit = strKey.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = cache.get(keySplit[2]);
		String value = (String) element.getObjectValue();
		return value.getBytes();
	}

	@Override
	public String setByte(byte[] key, byte[] value) {
		String strKey = new String(key);
		String[] keySplit = strKey.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = new Element(keySplit[2], new String(value));
		cache.put(element);
		return "OK";
	}

	@Override
	public void del(String... keys) {
		for (String key : keys) {
			try {
				String[] keySplit = key.split("_");
				Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
				Cache cache = cacheManager.getCache(keySplit[1]);
				cache.remove(keySplit[2]);
			} catch (Exception e) {

			}
		}
	}

	@Override
	public List<String> mget(String[] keys) {
		List<String> resultList = new ArrayList<>();
		for (String key : keys) {
			try {
				String[] keySplit = key.split("_");
				Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
				Cache cache = cacheManager.getCache(keySplit[1]);
				Element element = cache.get(keySplit[2]);
				String value = (String) element.getObjectValue();
				resultList.add(value);
			} catch (Exception e) {

			}
		}
		return resultList;
	}

	@Override
	public List<byte[]> mgetByte(byte[][] keys) {
		List<byte[]> resultList = new ArrayList<>();
		for (byte[] byteKey : keys) {
			try {
				String key = new String(byteKey);
				String[] keySplit = key.split("_");
				Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
				Cache cache = cacheManager.getCache(keySplit[1]);
				Element element = cache.get(keySplit[2]);
				String value = (String) element.getObjectValue();
				resultList.add(value.getBytes());
			} catch (Exception e) {

			}
		}
		return resultList;
	}

	@Override
	public Long pexpireAt(String cachedKey, long time) {
		return 0l;
	}

	@Override
	public boolean exists(String key) {
		String[] keySplit = key.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		return cache.isKeyInCache(keySplit[2]);
	}

	@Override
	public void setEx(String key, int seconds, String value) {
		String[] keySplit = key.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = new Element(keySplit[2], value);
		cache.put(element);
	}

	@Override
	public String set(String key, String value) {
		String[] keySplit = key.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = new Element(keySplit[2], value);
		cache.put(element);
		return value;
	}

}
