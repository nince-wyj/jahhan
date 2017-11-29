package net.jahhan.zookeeper.curator;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;

import net.jahhan.common.extension.utils.PropertiesUtil;
import net.jahhan.zookeeper.AbstractZookeeperClient;
import net.jahhan.zookeeper.ChildListener;
import net.jahhan.zookeeper.ChildNodeListener;
import net.jahhan.zookeeper.StateListener;

public class CuratorZookeeperClient extends AbstractZookeeperClient<CuratorWatcher> {

	private CuratorFramework client;

	public CuratorZookeeperClient() {
		super();
		String connectString = PropertiesUtil.get("base", "zk.host");
		if (StringUtils.isNotEmpty(connectString)) {
			try {
				CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString(connectString)
						.retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000)).connectionTimeoutMs(5000);
				String authority = PropertiesUtil.get("base", "zk.authority");
				if (authority != null && authority.length() > 0) {
					builder = builder.authorization("digest", authority.getBytes());
				}
				client = builder.build();
				client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
					public void stateChanged(CuratorFramework client, ConnectionState state) {
						if (state == ConnectionState.LOST) {
							CuratorZookeeperClient.this.stateChanged(StateListener.DISCONNECTED);
						} else if (state == ConnectionState.CONNECTED) {
							CuratorZookeeperClient.this.stateChanged(StateListener.CONNECTED);
						} else if (state == ConnectionState.RECONNECTED) {
							CuratorZookeeperClient.this.stateChanged(StateListener.RECONNECTED);
						}
					}
				});
				client.start();
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}

	}

	public void createPersistent(String path) {
		try {
			client.create().forPath(path);
		} catch (NodeExistsException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public void createEphemeral(String path) {
		try {
			client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
		} catch (NodeExistsException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public void delete(String path) {
		try {
			client.delete().forPath(path);
		} catch (NoNodeException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public List<String> getChildren(String path) {
		try {
			return client.getChildren().forPath(path);
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public boolean isConnected() {
		return client.getZookeeperClient().isConnected();
	}

	public void doClose() {
		client.close();
	}

	public CuratorWatcher createTargetChildListener(String path, ChildListener listener) {
		return new CuratorWatcherImpl(listener);
	}

	public List<String> addTargetChildListener(String path, CuratorWatcher listener) {
		try {
			return client.getChildren().usingWatcher(listener).forPath(path);
		} catch (NoNodeException e) {
			return null;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public void removeTargetChildListener(String path, CuratorWatcher listener) {
		((CuratorWatcherImpl) listener).unwatch();
	}

	private class CuratorWatcherImpl implements CuratorWatcher {

		private volatile ChildListener listener;

		public CuratorWatcherImpl(ChildListener listener) {
			this.listener = listener;
		}

		public void unwatch() {
			this.listener = null;
		}

		public void process(WatchedEvent event) throws Exception {
			if (listener != null) {
				listener.childChanged(event.getPath(),
						client.getChildren().usingWatcher(this).forPath(event.getPath()));
			}
		}
	}

	@SuppressWarnings("resource")
	@Override
	public void addChildNodeListener(String path, ChildNodeListener listener) {
		TreeCache cache = new TreeCache(client, path);
		cache.getListenable().addListener((curatorFramework, event) -> {
			if (null != event && null != event.getData() && null != event.getType()) {
				String servicePath = event.getData().getPath();
				if (servicePath.split("/").length > 3) {
					if (null != event.getData().getData()) {
						listener.childChanged(servicePath, event.getType().toString(),
								new String(event.getData().getData()));
					} else {
						listener.childChanged(servicePath, event.getType().toString(), null);
					}
				}
			}
		});
		try {
			cache.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createPersistent(String path, String data) {
		try {
			client.create().forPath(path, data.getBytes());
		} catch (NodeExistsException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void createEphemeral(String path, String data) {
		try {
			client.create().withMode(CreateMode.EPHEMERAL).forPath(path, data.getBytes());
		} catch (NodeExistsException e) {
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
