package net.jahhan.extension.broadCastSender;

import java.util.Set;

import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.constants.RedisConstants;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.context.BaseContext;
import net.jahhan.spi.common.BroadcastSender;

@Slf4j
@Extension("redisGlobalLock")
@Singleton
public class RedisBroadCastSender implements BroadcastSender {

	private static Redis getRedis() {
		return RedisFactory.getRedis(RedisConstants.GLOBAL_LOCK, null);
	}

	@Override
	public void send(String topic, String message) {
		Long result = getRedis().publish("__keyevent@0__:" + topic, topic + ":" + message);
		log.debug(topic + " message:" + message + " result:" + result);
	}

	@Override
	public void setChainNode(String chain) {
		getRedis().sadd(chain, BaseContext.CTX.getNode().getNodeId());
		getRedis().expire(chain, 3600 * 24);
	}

	@Override
	public void removeChainNode(String chain) {
		getRedis().srem(chain, BaseContext.CTX.getNode().getNodeId());
	}

	@Override
	public void removeChain(String chain) {
		getRedis().del(chain);
	}

	@Override
	public Set<String> getChainNode(String chain) {
		return getRedis().smembers(chain);
	}
}
