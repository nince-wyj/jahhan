package net.jahhan.init.initer;

import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.web.action.WorkHandlerHelper;

@InitAnnocation(isLazy = false, initSequence = 8000, onlyWeb = true)
public class ActionHandlerIniter implements BootstrapInit {

	@Override
	public void execute() {
		WorkHandlerHelper.init();
	}
}
