package net.jahhan.jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

/**
 * 高级哨兵池
 */
@Slf4j
public class JedisSentinelPoolExt extends Pool<Jedis> {

	protected GenericObjectPoolConfig poolConfig;
	protected int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
	protected String password;
	protected int database = Protocol.DEFAULT_DATABASE;
	protected String clientName;

	protected Set<MasterListener> masterListeners = new HashSet<>();
	protected Set<SlaveDownListener> slaveDownListeners = new HashSet<>();
	protected Set<SlaveUpListener> slaveUpListeners = new HashSet<>();

	protected List<HostAndPort> readHaps = new ArrayList<>();
	protected List<JedisPoolExt> jedisReadPools = new CopyOnWriteArrayList<>();
	protected Set<String> _sentinels = new LinkedHashSet<>();
	protected Map<HostAndPort, Integer> failMap = new ConcurrentHashMap<>();
	private String masterName;
	private int failMax;
	protected boolean masterWriteOnly;
	protected long subscribeRetryWaitTimeMillis = 5000;

	private volatile JedisFactory factory;
	private volatile HostAndPort currentHostMaster;

	public JedisSentinelPoolExt(String masterName, String sentinels, final GenericObjectPoolConfig poolConfig,
			int connectionTimeout, int failMax, boolean masterWriteOnly) {
		this(masterName, sentinels, poolConfig, null, connectionTimeout, failMax, masterWriteOnly, null);
	}

	public JedisSentinelPoolExt(String masterName, String sentinels, final GenericObjectPoolConfig poolConfig,
			int connectionTimeout, int failMax, boolean masterWriteOnly, String clientName) {
		this(masterName, sentinels, poolConfig, null, connectionTimeout, failMax, masterWriteOnly, clientName);
	}

	public JedisSentinelPoolExt(String masterName, String sentinels, final GenericObjectPoolConfig poolConfig,
			final String password, int connectionTimeout, int failMax, boolean masterWriteOnly) {
		this(masterName, sentinels, poolConfig, password, connectionTimeout, failMax, masterWriteOnly, null);
	}

	public JedisSentinelPoolExt(String masterName, String sentinels, final GenericObjectPoolConfig poolConfig,
			final String password, int connectionTimeout, int failMax, boolean masterWriteOnly, String clientName) {

		String[] strings = sentinels.split(",");
		Collections.addAll(_sentinels, strings);

		this.poolConfig = poolConfig;
		this.connectionTimeout = connectionTimeout;
		if (!StringUtils.isBlank(password)) {
			this.password = password;
		}
		this.database = Protocol.DEFAULT_DATABASE;
		this.failMax = failMax;
		this.masterName = masterName;
		this.masterWriteOnly = masterWriteOnly;
		if (!StringUtils.isBlank(clientName)) {
			this.clientName = clientName;
		}

		HostAndPort master = initSentinels(_sentinels, masterName);
		initPool(master);

		initReadPool();
	}

	private void initReadPool() {
		updateReadPools(readHaps);
	}

	/**
	 * 获取写实例
	 * 
	 * @return
	 */
	public WriteJedis getWriteResource() {
		return new WriteJedis(getResource());
	}

	/**
	 * 获取读实例
	 * 
	 * @return
	 */
	public ReadJedis getReadResource() {
		if (jedisReadPools.size() == 0) {
			throw new RedisException("there is no jedis for read");
		}

		// 从redis读池中随机获取一个实例
		Random rand = new Random();
		int randNum = rand.nextInt(jedisReadPools.size());
		JedisPoolExt jedisPoolExt = jedisReadPools.get(randNum);
		HostAndPort hostAndPort = jedisPoolExt.getHostAndPort();
		JedisPool jedisPool = jedisPoolExt.getJedisPool();
		ReadJedis readJedis;
		try {
			Jedis jedis = jedisPool.getResource();
			readJedis = new ReadJedis(jedis, jedisPool);
		} catch (Throwable e) {
			// 添加失败列表
			addFailMap(hostAndPort, jedisPool);

			// 重试其它连接池
			if (jedisReadPools.size() > 1) {
				while (true) {
					// 随机获取另一个读节点,仍然连接失败就抛异常
					int randNum1 = rand.nextInt(jedisReadPools.size());
					if (randNum1 != randNum) {
						JedisPoolExt jedisPoolExt1 = jedisReadPools.get(randNum1);
						JedisPool jedisPool1 = jedisPoolExt1.getJedisPool();
						try {
							Jedis jedis = jedisPool1.getResource();
							readJedis = new ReadJedis(jedis, jedisPool1);
						} catch (Exception e1) {
							addFailMap(hostAndPort, jedisPool);
							throw new RedisException("JedisSentinelPoolExt getReadResource retry error", e1);
						}
						return readJedis;
					}
				}
			} else {
				throw new RedisException(e);
			}
		}
		return readJedis;
	}

	/**
	 * 连接失败的实例,放入失败记录列表,超过指定失败次数的实例,会从读节点列表中丢弃
	 * 
	 * @param hostAndPort
	 * @param jedisPool
	 */
	private void addFailMap(HostAndPort hostAndPort, JedisPool jedisPool) {
		if (failMax != 0 && jedisReadPools.size() > 1) {
			Integer failTimes = failMap.get(hostAndPort);
			if (failTimes == null) {
				failTimes = 1;
			} else {
				failTimes++;
			}

			if (failTimes >= failMax) {
				failMap.remove(hostAndPort);
				jedisPool.close();
				removeFromReadPool(hostAndPort);
			} else {
				failMap.put(hostAndPort, failTimes);
			}
		}
	}

	public void resetReadPool() {
		initSentinels(_sentinels, masterName);
		initReadPool();
		failMap.clear();
	}

	public void destroy() {
		for (MasterListener m : masterListeners) {
			m.shutdown();
		}
		for (SlaveDownListener m : slaveDownListeners) {
			m.shutdown();
		}
		for (SlaveUpListener m : slaveUpListeners) {
			m.shutdown();
		}
		super.destroy();
	}

	public HostAndPort getCurrentHostMaster() {
		return currentHostMaster;
	}

	protected void initPool(HostAndPort master) {
		if (!master.equals(currentHostMaster)) {
			currentHostMaster = master;
			if (factory == null) {
				factory = new JedisFactory(master.getHost(), master.getPort(), connectionTimeout, password, database,
						clientName);
				initPool(poolConfig, factory);
			} else {
				factory.setHostAndPort(currentHostMaster);
				// although we clear the pool, we still have to check the
				// returned object
				// in getResource, this call only clears idle instances, not
				// borrowed instances
				internalPool.clear();
			}

			log.info("Created JedisPool to master at " + master);
		}
	}

	protected HostAndPort initSentinels(Set<String> sentinels, final String masterName) {

		HostAndPort master = null;
		boolean sentinelAvailable = false;

		log.info("Trying to find master from available Sentinels...");

		for (String sentinel : sentinels) {
			final HostAndPort hap = toHostAndPort(Arrays.asList(sentinel.split(":")));

			log.info("Connecting to Sentinel " + hap);

			Jedis jedis = null;
			try {
				jedis = new Jedis(hap.getHost(), hap.getPort());

				List<String> masterAddr = jedis.sentinelGetMasterAddrByName(masterName);

				// connected to sentinel...
				sentinelAvailable = true;

				if (masterAddr == null || masterAddr.size() != 2) {
					log.warn("Can not get master addr, master name: " + masterName + ". Sentinel: " + hap + ".");
					continue;
				}

				master = toHostAndPort(masterAddr);
				log.info("Found Redis master at " + master);

				initReadHaps(jedis, masterName, master);

				break;
			} catch (JedisConnectionException e) {
				log.warn("Cannot connect to sentinel running @ " + hap + ". Trying next one.");
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}

		if (master == null) {
			if (sentinelAvailable) {
				// can connect to sentinel, but master name seems to not
				// monitored
				throw new JedisException(
						"Can connect to sentinel, but " + masterName + " seems to be not monitored...");
			} else {
				throw new JedisConnectionException(
						"All sentinels down, cannot determine where is " + masterName + " master is running...");
			}
		}

		log.info("Redis master running at " + master + ", starting Sentinel listeners...");

		for (MasterListener masterListener : masterListeners) {
			masterListener.shutdown();
		}
		masterListeners.clear();

		for (SlaveDownListener slaveDownListener : slaveDownListeners) {
			slaveDownListener.shutdown();
		}
		slaveDownListeners.clear();

		for (SlaveUpListener slaveUpListener : slaveUpListeners) {
			slaveUpListener.shutdown();
		}
		slaveUpListeners.clear();

		for (String sentinel : sentinels) {
			final HostAndPort hap = toHostAndPort(Arrays.asList(sentinel.split(":")));
			MasterListener masterListener = new MasterListener(masterName, hap.getHost(), hap.getPort());
			masterListeners.add(masterListener);
			masterListener.start();

			SlaveDownListener slaveDownListener = new SlaveDownListener(masterName, hap.getHost(), hap.getPort());
			slaveDownListeners.add(slaveDownListener);
			slaveDownListener.start();

			SlaveUpListener slaveUpListener = new SlaveUpListener(masterName, hap.getHost(), hap.getPort());
			slaveUpListeners.add(slaveUpListener);
			slaveUpListener.start();

		}

		return master;
	}

	private void initReadHaps(Jedis jedis, String masterName, HostAndPort master) {
		List<Map<String, String>> slaveList = jedis.sentinelSlaves(masterName);
		initReadHaps(slaveList, master);
	}

	/**
	 * 初始化读池
	 * 
	 * @param slaveList
	 * @param master
	 * @return
	 */
	private List<HostAndPort> initReadHaps(List<Map<String, String>> slaveList, HostAndPort master) {
		// master允许读时,才放入读池
		if (!masterWriteOnly) {
			readHaps = createReadHaps(slaveList, master);
		} else {
			readHaps = createReadHaps(slaveList);
		}
		return readHaps;
	}

	/**
	 * 将slave节点作为读节点放入读池
	 * 
	 * @param slaveList
	 * @return
	 */
	private List<HostAndPort> createReadHaps(List<Map<String, String>> slaveList) {
		List<HostAndPort> haps = new ArrayList<>();

		for (Map<String, String> slave : slaveList) {
			HostAndPort hap = new HostAndPort(slave.get("ip"), Integer.valueOf(slave.get("port")));
			haps.add(hap);
		}

		return haps;
	}

	private List<HostAndPort> createReadHaps(List<Map<String, String>> slaveList, HostAndPort master) {
		List<HostAndPort> haps = createReadHaps(slaveList);
		haps.add(master);
		return haps;
	}

	/**
	 * 读池增加新节点
	 * 
	 * @param hostAndPort
	 */
	protected synchronized void insertReadPool(HostAndPort hostAndPort) {
		boolean exists = false;
		for (JedisPoolExt oldJedisPool : jedisReadPools) {
			if (oldJedisPool.getHostAndPort().equals(hostAndPort)) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			JedisPoolExt newJedisPool = new JedisPoolExt(poolConfig, hostAndPort.getHost(), hostAndPort.getPort(),
					connectionTimeout, password, Protocol.DEFAULT_DATABASE, clientName);
			jedisReadPools.add(newJedisPool);

			// 新来的节点,可能是之前挂掉后恢复的节点,所以要检查其是否存在于失败列表中,要去除
			failMap.remove(hostAndPort);

			log.info("Add JedisReadPool at " + hostAndPort);
		}
	}

	/**
	 * 读池中去掉一个读节点
	 * 
	 * @param hostAndPort
	 */
	protected synchronized void removeFromReadPool(HostAndPort hostAndPort) {
		for (JedisPoolExt jedisPoolExt : jedisReadPools) {
			if (jedisPoolExt.getHostAndPort().equals(hostAndPort)) {
				jedisReadPools.remove(jedisPoolExt);
				log.info("Remove JedisReadPool at " + hostAndPort);
				break;
			}
		}
	}

	/**
	 * 替换读池
	 * 
	 * @param newHaps
	 */
	protected synchronized void updateReadPools(List<HostAndPort> newHaps) {

		// 拿来新的读节点,在老池里补上
		for (HostAndPort hap : newHaps) {
			insertReadPool(hap);
		}

		// 把老池里有,但不存在于新节点里的节点去掉
		for (int i = 0; i < jedisReadPools.size(); i++) {
			boolean needRemove = true;
			for (HostAndPort hap : newHaps) {
				if (jedisReadPools.get(i).getHostAndPort().equals(hap)) {
					needRemove = false;
					break;
				}
			}
			if (needRemove) {
				jedisReadPools.remove(i);
				i--;
			}
		}
	}

	protected HostAndPort toHostAndPort(List<String> getMasterAddrByNameResult) {
		String host = getMasterAddrByNameResult.get(0);
		int port = Integer.parseInt(getMasterAddrByNameResult.get(1));
		return new HostAndPort(host, port);
	}

	@Override
	public Jedis getResource() {
		while (true) {
			Jedis jedis = super.getResource();
			jedis.setDataSource(this);

			// get a reference because it can change concurrently
			final HostAndPort master = currentHostMaster;
			final HostAndPort connection = new HostAndPort(jedis.getClient().getHost(), jedis.getClient().getPort());

			if (master.equals(connection)) {
				// connected to the correct master
				return jedis;
			} else {
				jedis.close();
			}
		}
	}

	/**
	 * @deprecated starting from Jedis 3.0 this method won't exist. Resouce
	 *             cleanup should be done using @see {@link Jedis#close()}
	 */
	public void returnBrokenResource(final Jedis resource) {
		if (resource != null) {
			returnBrokenResourceObject(resource);
		}
	}

	/**
	 * @deprecated starting from Jedis 3.0 this method won't exist. Resouce
	 *             cleanup should be done using @see {@link Jedis#close()}
	 */
	public void returnResource(final Jedis resource) {
		if (resource != null) {
			resource.resetState();
			returnResourceObject(resource);
		}
	}

	/**
	 * master切换事件监听
	 */
	protected class MasterListener extends Thread {

		protected String masterName;
		protected String host;
		protected int port;
		protected Jedis j;
		protected AtomicBoolean running = new AtomicBoolean(false);

		public MasterListener(String masterName, String host, int port) {
			this.masterName = masterName;
			this.host = host;
			this.port = port;
		}

		public void run() {

			running.set(true);

			while (running.get()) {

				j = new Jedis(host, port);

				try {
					j.subscribe(new JedisPubSub() {
						@Override
						public void onMessage(String channel, String message) {
							log.info("Sentinel " + host + ":" + port + " published: " + channel + " " + message + ".");

							String[] switchMasterMsg = message.split(" ");

							if (switchMasterMsg.length > 3) {

								if (masterName.equals(switchMasterMsg[0])) {
									initPool(toHostAndPort(Arrays.asList(switchMasterMsg[3], switchMasterMsg[4])));

									if (masterWriteOnly) {
										// 如果master只做写,则将新的master从读池去掉
										removeFromReadPool(
												toHostAndPort(Arrays.asList(switchMasterMsg[3], switchMasterMsg[4])));
									} else {
										// 如果master同时允许读写,则将旧master从读池去掉
										removeFromReadPool(
												toHostAndPort(Arrays.asList(switchMasterMsg[1], switchMasterMsg[2])));
									}
								} else {
									log.info("Ignoring message on +switch-master for master name " + switchMasterMsg[0]
											+ ", our master name is " + masterName);
								}

							} else {
								log.warn("Invalid message received on Sentinel " + host + ":" + port
										+ " on channel +switch-master: " + message);
							}

						}
					}, "+switch-master");
				} catch (JedisConnectionException e) {
					runningSleep(running.get(), host, port, subscribeRetryWaitTimeMillis);
				}
			}
		}

		public void shutdown() {
			shutdownListener(j, running, host, port);
		}
	}

	/**
	 * slave故障恢复&新增slave节点 事件监听
	 */
	protected class SlaveUpListener extends Thread {

		protected String masterName;
		protected String host;
		protected int port;
		protected Jedis j;
		protected AtomicBoolean running = new AtomicBoolean(false);

		public SlaveUpListener(String masterName, String host, int port) {
			this.masterName = masterName;
			this.host = host;
			this.port = port;
		}

		public void run() {
			running.set(true);

			while (running.get()) {

				j = new Jedis(host, port);

				try {
					j.subscribe(new JedisPubSub() {
						@Override
						public void onMessage(String channel, String message) {
							log.info("Sentinel " + host + ":" + port + " published: " + channel + " " + message + ".");

							String[] switchMasterMsg = message.split(" ");

							if (switchMasterMsg.length > 7) {
								String slaveHost = switchMasterMsg[2];
								int slavePort = Integer.valueOf(switchMasterMsg[3]);
								HostAndPort hap = new HostAndPort(slaveHost, slavePort);
								insertReadPool(hap);
							} else {
								log.warn("Invalid message received on Sentinel " + host + ":" + port + " on channel "
										+ channel + ": " + message);
							}
						}
					}, "-sdown", "+slave");
				} catch (JedisConnectionException e) {
					runningSleep(running.get(), host, port, subscribeRetryWaitTimeMillis);
				}
			}
		}

		public void shutdown() {
			shutdownListener(j, running, host, port);
		}
	}

	protected void shutdownListener(Jedis j, AtomicBoolean running, String host, int port) {
		try {
			log.info("Shutting down listener on " + host + ":" + port);
			running.set(false);
			j.disconnect();
		} catch (Exception e) {
			log.error("Caught exception while shutting down: ", e);
		}
	}

	protected void runningSleep(boolean running, String host, int port, long waitTimeMillis) {
		if (running) {
			log.warn("Lost connection to Sentinel at " + host + ":" + port + ". Sleeping " + waitTimeMillis
					+ "ms and retrying.");
			try {
				Thread.sleep(waitTimeMillis);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		} else {
			log.warn("Unsubscribing from Sentinel at " + host + ":" + port);
		}
	}

	/**
	 * slave挂掉事件监听
	 */
	protected class SlaveDownListener extends Thread {

		protected String masterName;
		protected String host;
		protected int port;
		protected Jedis j;
		protected AtomicBoolean running = new AtomicBoolean(false);

		public SlaveDownListener(String masterName, String host, int port) {
			this.masterName = masterName;
			this.host = host;
			this.port = port;
		}

		public void run() {

			running.set(true);

			while (running.get()) {

				j = new Jedis(host, port);

				try {
					j.subscribe(new JedisPubSub() {
						@Override
						public void onMessage(String channel, String message) {
							log.info("Sentinel " + host + ":" + port + " published: " + channel + " " + message + ".");

							String[] switchMasterMsg = message.split(" ");

							if (switchMasterMsg.length > 7) {
								String slaveHost = switchMasterMsg[2];
								int slavePort = Integer.valueOf(switchMasterMsg[3]);
								HostAndPort hap = new HostAndPort(slaveHost, slavePort);
								removeFromReadPool(hap);
							} else if (switchMasterMsg.length != 4) {
								// master +sdown length=4
								log.error("Invalid message received on Sentinel " + host + ":" + port
										+ " on channel +sdown: " + message);
							}
						}
					}, "+sdown");
				} catch (JedisConnectionException e) {
					runningSleep(running.get(), host, port, subscribeRetryWaitTimeMillis);
				}
			}
		}

		public void shutdown() {
			shutdownListener(j, running, host, port);
		}
	}

}