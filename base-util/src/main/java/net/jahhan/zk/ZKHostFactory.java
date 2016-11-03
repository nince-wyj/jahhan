package net.jahhan.zk;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKHostFactory {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private CuratorFramework zkClient;

	private List<ListenerHandler> listeners = new ArrayList<>();
	private String namespace;
	private String zkhost;
	private String hostType;
	private String thisHost;

	private Integer maxRetries = 20;
	private Integer baseSleepTimeMs = 5000;
	private Integer connectionTimeoutMs = 10000;
	private Integer sessionTimeoutMs = 10000;

	public ZKHostFactory(String namespace, String zkhost, String hostType, String thisHost) {
		super();
		this.namespace = namespace;
		this.zkhost = zkhost;
		this.hostType = hostType;
		this.thisHost = thisHost;
	}

	// 设置Zookeeper启动后需要调用的监听或者，或者需要做的初始化工作。
	public void setListeners(List<ListenerHandler> listeners) {
		this.listeners = listeners;
	}

	public void setMaxRetries(Integer maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void setBaseSleepTimeMs(Integer baseSleepTimeMs) {
		this.baseSleepTimeMs = baseSleepTimeMs;
	}

	public void setConnectionTimeoutMs(Integer connectionTimeoutMs) {
		this.connectionTimeoutMs = connectionTimeoutMs;
	}

	public void setSessionTimeoutMs(Integer sessionTimeoutMs) {
		this.sessionTimeoutMs = sessionTimeoutMs;
	}

	public void init() throws Exception {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries);
		zkClient = createWithOptions(zkhost, retryPolicy, connectionTimeoutMs, sessionTimeoutMs);
		registerListeners(zkClient);
		zkClient.start();
		if (thisHost.equals("/")) {
			logger.error("inner ip is null!!");
			throw new Exception("inner ip is null!!");
		}
		if (null == zkClient.checkExists().forPath("/" + hostType)) {
			zkClient.create().forPath("/" + hostType);
		}
		logger.debug("thisHost: " + thisHost);
		zkClient.create().withMode(CreateMode.EPHEMERAL).forPath("/" + hostType + thisHost);
	}

	public CuratorFramework createWithOptions(String connectionString, RetryPolicy retryPolicy, int connectionTimeoutMs,
			int sessionTimeoutMs) {
		return CuratorFrameworkFactory.builder().namespace(namespace).connectString(connectionString)
				.retryPolicy(retryPolicy).connectionTimeoutMs(connectionTimeoutMs).sessionTimeoutMs(sessionTimeoutMs)
				.build();
	}

	// 注册需要监听的监听者对像.
	private void registerListeners(CuratorFramework client) {
		client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				logger.info("CuratorFramework state changed: {}", newState);
				if (newState == ConnectionState.CONNECTED || newState == ConnectionState.RECONNECTED) {
					for (ListenerHandler listener : listeners) {
						listener.executor(client);
						logger.info("Listener {} executed!", listener.getClass().getName());
					}
				}
			}
		});

		client.getUnhandledErrorListenable().addListener(new UnhandledErrorListener() {
			@Override
			public void unhandledError(String message, Throwable e) {
				logger.info("CuratorFramework unhandledError: {}", message);
			}
		});
	}

	public CuratorFramework getClient() {
		return zkClient;
	}

	public void destroy() throws Exception {
		zkClient.close();
	}

}