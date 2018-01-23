package net.jahhan.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;

import com.google.inject.Injector;

import lombok.Getter;
import lombok.Setter;

/**
 * @author nince
 */
@Singleton
public class BaseContext {

	public BaseContext() {
		if (null == CTX) {
			CTX = this;
		}
	}

	public static BaseContext CTX;
	@Inject
	private Injector injector;
	@Inject
	private ThreadLocalUtil<VariableContext> threadLocalUtil;
	@Getter
	private Node node = Node.getInstance();
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

	public Injector getInjector() {
		return injector;
	}

	public ThreadLocalUtil<VariableContext> getThreadLocalUtil() {
		return threadLocalUtil;
	}

	public VariableContext getVariableContext() {
		return threadLocalUtil.getValue();
	}
}
