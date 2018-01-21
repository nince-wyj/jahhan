package net.jahhan.extension.ehcache;

import java.util.ArrayList;
import java.util.List;

import net.jahhan.cache.ehcache.EhcacheFactory;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.JahhanErrorCode;
import net.jahhan.common.extension.utils.Assert;
import net.jahhan.spi.DBCache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Extension("ehcache")
public class DBEhcacheWapper implements DBCache {

	@Override
	public void setEx(byte[] key, int seconds, byte[] value) {
		CacheManager cacheManager = EhcacheFactory.getCacheManager();
		String strKey = new String(key);
		String[] keySplit = strKey.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = new Element(keySplit[2], value);
		cache.put(element);
	}

	@Override
	public void expire(String cachedKey, int seconds) {
		CacheManager cacheManager = EhcacheFactory.getCacheManager();
		String[] keySplit = cachedKey.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		cache.get(keySplit[2]);
	}

	@Override
	public String get(String key) {
		CacheManager cacheManager = EhcacheFactory.getCacheManager();
		String[] keySplit = key.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = cache.get(keySplit[2]);
		return null == element ? null : (String) element.getObjectValue();
	}

	@Override
	public byte[] getBinary(byte[] key) {
		CacheManager cacheManager = EhcacheFactory.getCacheManager();
		String strKey = new String(key);
		String[] keySplit = strKey.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = cache.get(keySplit[2]);
		return null == element ? null : (byte[]) element.getObjectValue();
	}

	@Override
	public String setByte(byte[] key, byte[] value) {
		CacheManager cacheManager = EhcacheFactory.getCacheManager();
		String strKey = new String(key);
		String[] keySplit = strKey.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = new Element(keySplit[2], value);
		cache.put(element);
		return "OK";
	}

	@Override
	public Long del(String... keys) {
		CacheManager cacheManager = EhcacheFactory.getCacheManager();
		long num = 0l;
		for (String key : keys) {
			try {
				String[] keySplit = key.split("_");
				Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
				Cache cache = cacheManager.getCache(keySplit[1]);
				cache.remove(keySplit[2]);
				num++;
			} catch (Exception e) {

			}
		}
		return num;
	}

	@Override
	public List<String> mget(String[] keys) {
		CacheManager cacheManager = EhcacheFactory.getCacheManager();
		List<String> resultList = new ArrayList<>();
		for (String key : keys) {
			try {
				String[] keySplit = key.split("_");
				Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
				Cache cache = cacheManager.getCache(keySplit[1]);
				Element element = cache.get(keySplit[2]);
				if (null != element) {
					resultList.add((String) element.getObjectValue());
				}
			} catch (Exception e) {

			}
		}
		return resultList;
	}

	@Override
	public List<byte[]> mgetByte(byte[][] keys) {
		CacheManager cacheManager = EhcacheFactory.getCacheManager();
		List<byte[]> resultList = new ArrayList<>();
		for (byte[] byteKey : keys) {
			try {
				String key = new String(byteKey);
				String[] keySplit = key.split("_");
				Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
				Cache cache = cacheManager.getCache(keySplit[1]);
				Element element = cache.get(keySplit[2]);
				if (null != element) {
					resultList.add((byte[]) element.getObjectValue());
				}

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
		CacheManager cacheManager = EhcacheFactory.getCacheManager();
		String[] keySplit = key.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		return cache.isKeyInCache(keySplit[2]);
	}

	@Override
	public void setEx(String key, int seconds, String value) {
		CacheManager cacheManager = EhcacheFactory.getCacheManager();
		String[] keySplit = key.split("_");
		Assert.isTrue(keySplit.length > 2, "key错误", JahhanErrorCode.UNKNOW_ERROR);
		Cache cache = cacheManager.getCache(keySplit[1]);
		Element element = new Element(keySplit[2], value);
		cache.put(element);
	}

}
