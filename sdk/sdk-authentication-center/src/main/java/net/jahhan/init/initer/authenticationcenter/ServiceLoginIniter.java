package net.jahhan.init.initer.authenticationcenter;

import javax.inject.Inject;

import net.jahhan.controller.authenticationcenter.ServiceLogin;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 1700)
public class ServiceLoginIniter implements BootstrapInit {
	@Inject
	private ServiceLogin serviceLogin;

	@Override
	public void execute() {
		serviceLogin.loginInit();
	}
}
