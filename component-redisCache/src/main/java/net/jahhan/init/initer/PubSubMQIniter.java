package net.jahhan.init.initer;

import javax.inject.Inject;

import com.google.inject.Injector;

import net.jahhan.cache.mq.MqPubSubRegister;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(initOverWait = false)
public class PubSubMQIniter implements BootstrapInit {
	@Inject
	private Injector injector;

	@Override
	public void execute() {
		if (SysConfiguration.getMqActualize().equals("redis")) {
			injector.getInstance(MqPubSubRegister.class).scan();
		}
	}
}
