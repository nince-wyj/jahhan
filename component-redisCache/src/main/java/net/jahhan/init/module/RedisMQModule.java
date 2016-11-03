package net.jahhan.init.module;

import com.google.inject.AbstractModule;

import net.jahhan.cache.mq.MqProConSender;
import net.jahhan.cache.mq.MqPubSubSender;
import net.jahhan.cache.mq.MqSafeProConSender;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.init.InitAnnocation;
import net.jahhan.mq.MQSendService;
import net.jahhan.mq.annotation.MqProCon;
import net.jahhan.mq.annotation.MqPubsub;
import net.jahhan.mq.annotation.MqSafeProCon;

@InitAnnocation(isLazy = false, initSequence = 1100)
public class RedisMQModule extends AbstractModule {

	@Override
	protected void configure() {
		if (SysConfiguration.getMqActualize().equals("redis")){
			bind(MQSendService.class).annotatedWith(MqPubsub.class).to(MqPubSubSender.class);
			bind(MQSendService.class).annotatedWith(MqProCon.class).to(MqProConSender.class);
			bind(MQSendService.class).annotatedWith(MqSafeProCon.class).to(MqSafeProConSender.class);
		}
	}

}
