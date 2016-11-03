package net.jahhan.init.initer;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.db.conn.DBConnFactory;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 2000)
public class DBConnFactoryIniter implements BootstrapInit {
	@Override
	public void execute() {
		if (SysConfiguration.getUseSQLDB()) {
			// 数据库连接工厂初始化
			DBConnFactory.init();
		}
	}
}
