package net.jahhan.zk.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.cache.ClusterMap;
import net.jahhan.zk.ListenerHandler;

public class HostListener implements ListenerHandler {

	private static Logger logger = LoggerFactory.getLogger(HostListener.class);

	private String path = "/";
	private TreeCache cache;


	@Override
	public void executor(CuratorFramework client) {
		cache = new TreeCache(client, path);
		cache.getListenable().addListener(
				(curatorFramework, event) -> {
					logger.debug("event: {}", event);
					switch (event.getType()) {
					case NODE_ADDED: {
						String path = event.getData().getPath();
						String[] pathSplit = path.split("/");
						if (pathSplit.length > 2) {
							ClusterMap.getInstance().addServer(pathSplit[1],
									pathSplit[2]);
							logger.debug("Node added: {}", path);
						}
						break;
					}
					case NODE_REMOVED: {
						String host = event.getData().getPath();
						String[] pathSplit = path.split("/");
						if (pathSplit.length > 2) {
							ClusterMap.getInstance().deleteServer(pathSplit[1],
									pathSplit[2]);
							logger.debug("Node delete: {}", host);
						}
						break;
					}
					default:
						break;
					}
				});

		try {
			cache.start();
		} catch (Exception e) {
			logger.error(
					"Start PathChildrenCache error for path: {}, error info: {}",
					path, e.getMessage());
		}
	}
}
