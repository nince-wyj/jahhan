package net.jahhan.init.initer.jdbc;

import javax.inject.Inject;

import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.spi.DataSourcePoolInit;

@InitAnnocation(isLazy = false, initSequence = 2000)
public class DBConnFactoryIniter implements BootstrapInit {
	@Inject
	private DataSourcePoolInit dataSourcePoolInit;

	@Override
	public void execute() {
		// 数据库连接池初始化
		dataSourcePoolInit.init();
	}
}
