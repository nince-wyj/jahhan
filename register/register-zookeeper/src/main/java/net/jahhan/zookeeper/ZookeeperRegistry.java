package net.jahhan.zookeeper;

import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.common.extension.utils.JsonUtil;
import net.jahhan.context.BaseContext;
import net.jahhan.context.Node;
import net.jahhan.exception.JahhanException;
import net.jahhan.register.ClusterMessageHolder;
import net.jahhan.register.api.FailbackRegistry;
import net.jahhan.spi.JahhanZookeeperTransporter;

@Slf4j
public class ZookeeperRegistry extends FailbackRegistry {

	private final static int DEFAULT_ZOOKEEPER_PORT = 2181;

	@Getter
	private ZookeeperClient zkClient;
	@Inject
	private JahhanZookeeperTransporter zookeeperTransporter;
	@Inject
	private ClusterMessageHolder clusterMessageHolder;

	public void init(Node node) {
		this.node = node;
		zkClient = zookeeperTransporter.connect();
		zkClient.create(toPath(), JsonUtil.toJson(node), true);
		zkClient.addChildNodeListener("/node", new ChildNodeListener() {
			@Override
			public void childChanged(String path, String type, String value) {
				switch (type) {
				case "NODE_ADDED": {
					String[] pathSplit = path.split("/");
					if (pathSplit.length > 3) {
						clusterMessageHolder.addServer(pathSplit[2], pathSplit[3],
								JsonUtil.fromJson(value, Node.class));
					}
					break;
				}
				case "NODE_REMOVED": {
					String[] pathSplit = path.split("/");
					if (pathSplit.length > 3) {
						ClusterMessageHolder instance = BaseContext.CTX.getInjector()
								.getInstance(ClusterMessageHolder.class);
						instance.deleteServer(pathSplit[2], pathSplit[3]);
					}
					break;
				}
				default:
					break;
				}

			}
		});
	}

	public boolean isAvailable() {
		return zkClient.isConnected();
	}

	public void doDestroy() {
		super.destroy();
		try {
			zkClient.close();
		} catch (Exception e) {
			log.warn("Failed to close zookeeper client , cause: " + e.getMessage(), e);
		}
	}

	protected void doRegister() {
		try {
			zkClient.create(toPath(), true);
		} catch (Throwable e) {
			throw new JahhanException("Failed to register node to zookeeper , cause: " + e.getMessage(), e);
		}
	}

	protected void doUnregister() {
		try {
			zkClient.delete(toPath());
		} catch (Throwable e) {
			throw new JahhanException("Failed to unregister node to zookeeper, cause: " + e.getMessage(), e);
		}
	}

	static String appendDefaultPort(String address) {
		if (address != null && address.length() > 0) {
			int i = address.indexOf(':');
			if (i < 0) {
				return address + ":" + DEFAULT_ZOOKEEPER_PORT;
			} else if (Integer.parseInt(address.substring(i + 1)) == 0) {
				return address.substring(0, i + 1) + DEFAULT_ZOOKEEPER_PORT;
			}
		}
		return address;
	}

	private String toPath() {
		return "/node/" + BaseConfiguration.SERVICE + "/" + node.getNodeId();
	}
}