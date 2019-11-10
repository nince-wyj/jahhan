package net.jahhan.variable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.jahhan.common.extension.annotation.GlobalVariable;
import net.jahhan.common.extension.context.Variable;

@Data
@EqualsAndHashCode(callSuper = false)
@GlobalVariable("base")
public class BaseGlobalVariable extends Variable {
	private Map<String, Thread> chainMap = new ConcurrentHashMap<>();
	@Getter
	@Setter
	private String token;
	@Getter
	@Setter
	private String innerSecrityKey;
	@Getter
	@Setter
	private String appPubKey;
	@Getter
	@Setter
	private String thirdPubKey;
	@Getter
	@Setter
	private String browserSecrityKey;
	@Getter
	@Setter
	private String browserPubKey;
	@Getter
	@Setter
	private String firstSingleToken;

	public void setChain(String chainId, Thread t) {
		chainMap.put(chainId, t);
	}

	public Thread getChainThread(String chainId) {
		return chainMap.get(chainId);
	}

	public void removeChain(String chainId) {
		chainMap.remove(chainId);
	}

	public boolean containsChain(String chainId) {
		return chainMap.containsKey(chainId);
	}


}
