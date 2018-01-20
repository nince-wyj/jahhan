package net.jahhan.init.initer;

import java.util.List;

import javax.inject.Inject;

import com.google.inject.Injector;

import net.jahhan.cache.ehcache.EhcacheConstants;
import net.jahhan.cache.ehcache.EhcacheFactory;
import net.jahhan.common.extension.utils.ClassScaner;
import net.jahhan.common.extension.utils.PackageUtil;
import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.spi.DBEventListener;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.CacheConfiguration.BootstrapCacheLoaderFactoryConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

@InitAnnocation(isLazy = false, initSequence = 1100)
public class EhCacheIniter implements BootstrapInit {
	@Inject
	private Injector injector;

	@Override
	public void execute() {
		Configuration configuration = new Configuration();
		String[] packages = PackageUtil.packages("dao.listen");
		List<String> classNameList = new ClassScaner().parse(packages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String className : classNameList) {
			try {
				Class<?> clazz = classLoader.loadClass(className);
				DBEventListener instance = (DBEventListener) injector.getInstance(clazz);
				CacheConfiguration cacheConfiguration = new CacheConfiguration(clazz.getSimpleName(), 1000)
						.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)// 清理机制：LRU最近最少使用
																					// FIFO先进先出
																					// LFU较少使用
						.timeToIdleSeconds(instance.getExistSecond())// 元素最大闲置时间
						.eternal(false);// 元素是否永久缓存
				configuration.addCache(cacheConfiguration);
			} catch (ClassNotFoundException e) {
			}
		}
		System.setProperty(net.sf.ehcache.CacheManager.ENABLE_SHUTDOWN_HOOK_PROPERTY, "true");
		// 序列库初始化
		CacheConfiguration cacheConfiguration = new CacheConfiguration(EhcacheConstants.SEQ_CACHE, 1)
				.diskPersistent(true).memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU).eternal(true)
				.maxEntriesLocalDisk(0);
		configuration.addCache(cacheConfiguration);
		DiskStoreConfiguration diskStoreConfigurationParameter = new DiskStoreConfiguration();
		diskStoreConfigurationParameter.setPath(PropertiesUtil.get("base", "cachePath"));
		configuration.diskStore(diskStoreConfigurationParameter);
		CacheManager cacheManager = CacheManager.create(configuration);
		EhcacheFactory.init(cacheManager);
	}
}
