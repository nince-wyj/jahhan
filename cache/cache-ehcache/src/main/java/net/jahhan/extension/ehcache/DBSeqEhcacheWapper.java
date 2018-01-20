package net.jahhan.extension.ehcache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.jahhan.cache.ehcache.EhcacheConstants;
import net.jahhan.cache.ehcache.EhcacheFactory;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.DBSeqCache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

@Extension("ehcache")
public class DBSeqEhcacheWapper implements DBSeqCache {
	private Cache cache = EhcacheFactory.getCacheManager().getCache(EhcacheConstants.SEQ_CACHE);
	private Lock lock = new ReentrantLock();

	@Override
	public long incr(String key) {
		long valueResult;
		lock.lock();
		try {
			Element element = cache.get(key);
			if (null == element) {
				valueResult = 1;
			} else {
				long value = (long) element.getObjectValue();
				valueResult = ++value;
			}
			Element elementResult = new Element(key, valueResult);
			cache.put(elementResult);
		} finally {
			lock.unlock();
		}
		return valueResult;
	}

	@Override
	public long incrBy(String key, long addValue) {
		long valueResult;
		lock.lock();
		try {
			Element element = cache.get(key);
			if (null == element) {
				valueResult = addValue;
			} else {
				long value = (long) element.getObjectValue();
				valueResult = value + addValue;
			}
			Element elementResult = new Element(key, valueResult);
			cache.put(elementResult);
		} finally {
			lock.unlock();
		}
		return valueResult;
	}

	@Override
	public void set(String key, long value) {
		lock.lock();
		try {
			Element element = cache.get(key);
			if (null == element) {
				Element elementResult = new Element(key, value);
				cache.put(elementResult);
			} else {
				long oldValue = (long) element.getObjectValue();
				if (oldValue < value) {
					Element elementResult = new Element(key, value);
					cache.put(elementResult);
				}
			}
		} finally {
			lock.unlock();
		}
	}

}
