package net.jahhan.init.initer;

import net.jahhan.constant.SysConfiguration;
import net.jahhan.db.ConnectionHelper;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 3000)
public class ConnectionManagerIniter implements BootstrapInit {
	@Override
	public void execute() {
		if (SysConfiguration.getUseSQLDB()) {
			// 数据库连接工厂初始化
			ConnectionHelper.init();
		}
	}
}
