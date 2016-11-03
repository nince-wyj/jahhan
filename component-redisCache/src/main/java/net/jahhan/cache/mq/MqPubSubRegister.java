package net.jahhan.cache.mq;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.mq.MqScaner;
import net.jahhan.utils.PropertiesUtil;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class MqPubSubRegister extends MqScaner {

	@Override
	protected void register() {
		if(pubSublisteners.keySet().size()>0){
			new MqRepository(pubSublisteners.keySet().toArray(new String[pubSublisteners.keySet().size()])) {
				@Override
				public void onMessage(String channel, String msg) {
					try {
						pubSubMsgHandle(channel, msg);
					} catch (Exception e) {
						logger.error("", e);
					}
				}
			};
		}
	}

	public abstract class MqRepository extends JedisPubSub {
		protected Logger logger = LoggerFactory.getLogger(MqRepository.class);
		private String pre = "mq.";
		protected Client client;

		public MqRepository(String... channels) {
			if (channels.length > 0) {
				Properties is = PropertiesUtil.getProperties("redis");
				int database = Integer.parseInt(is.getProperty(pre + "database", "0"));
				int port = Integer.parseInt(is.getProperty(pre + "port"));
				String host = is.getProperty(pre + "host");
				@SuppressWarnings("resource")
				Jedis jedis = new Jedis(host, port, database);
				client = jedis.getClient();
				client.setPassword(is.getProperty(pre + "password"));
				this.proceed(client, channels);
			}

		}

		@Override
		public abstract void onMessage(String channel, String message);
	}
}
