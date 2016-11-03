package net.jahhan.init.initer;

import net.jahhan.dfs.FileSys;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 5000)
public class FilsysIniter implements BootstrapInit {
	public void execute() {
		FileSys.init();
	}
}
