package net.jahhan.spi;

import java.util.Set;

import net.jahhan.common.extension.annotation.SPI;

@SPI("redisGlobalLock")
public interface BroadcastSender {

	public void send(String topic, String message);

	public void setChainNode(String chain);
	
	public Set<String> getChainNode(String chain);

	public void removeChainNode(String chain);

	public void removeChain(String chain);
}
