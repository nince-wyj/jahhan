package net.jahhan.init.initer;

import net.jahhan.cache.RedisFactory;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 1100)
public class CacheFactoryIniter implements BootstrapInit {

	@Override
	public void execute() {
		// 启动初始化
		RedisFactory.init();
	}
}
