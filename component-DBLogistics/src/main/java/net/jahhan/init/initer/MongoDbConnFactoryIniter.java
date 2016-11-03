package net.jahhan.init.initer;

import java.util.List;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

import net.jahhan.dblogistics.doc.CodecRegistryHolder;
import net.jahhan.dblogistics.doc.DocClientFactory;
import net.jahhan.dblogistics.doc.DocCodec;
import net.jahhan.dblogistics.entity.SuperEntity;
import net.jahhan.init.BootstrapInit;
import net.jahhan.init.InitAnnocation;
import net.jahhan.utils.ClassScaner;
import net.jahhan.utils.PropertiesUtil;

@InitAnnocation(isLazy = false, initSequence = 2010)
public class MongoDbConnFactoryIniter implements BootstrapInit {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void execute() {
		MongoClientOptions.Builder build = new MongoClientOptions.Builder();
		// 与数据最大连接数50
		String connectionsPerHost = PropertiesUtil.get("dblogistic", "doc.connectionsPerHost");
		if (null != connectionsPerHost) {
			build.connectionsPerHost(Integer.valueOf(connectionsPerHost));
		}
		// 如果当前所有的connection都在使用中，则每个connection上可以有50个线程排队等待
		String threadsAllowedToBlockForConnectionMultiplier = PropertiesUtil.get("dblogistic",
				"doc.threadsAllowedToBlockForConnectionMultiplier");
		if (null != threadsAllowedToBlockForConnectionMultiplier) {
			build.threadsAllowedToBlockForConnectionMultiplier(
					Integer.valueOf(threadsAllowedToBlockForConnectionMultiplier));
		}
		String connectTimeout = PropertiesUtil.get("dblogistic", "doc.connectTimeout");
		if (null != connectTimeout) {
			build.connectTimeout(Integer.valueOf(connectTimeout));
		}
		String maxWaitTime = PropertiesUtil.get("dblogistic", "doc.maxWaitTime");
		if (null != maxWaitTime) {
			build.maxWaitTime(Integer.valueOf(maxWaitTime));
		}
		MongoClientOptions options = build.build();
		MongoClient client = new MongoClient(PropertiesUtil.get("dblogistic", "doc.uri"), options);
		DocClientFactory.getInstance().setClient(client);

		String pkg = PropertiesUtil.get("dblogistic", "entity.packages");
		String[] packages = new String[] { pkg + ".node", pkg + ".relation" };

		List<String> classNameList = new ClassScaner().parse(packages);
		Codec<? extends SuperEntity>[] codecs = new Codec[classNameList.size()];
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (int i = 0; i < classNameList.size(); i++) {
			String className = classNameList.get(i);
			try {
				Class<?> clazz = classLoader.loadClass(className);
				if (SuperEntity.class.isAssignableFrom(clazz)) {
					Codec<? extends SuperEntity> codec = new DocCodec(clazz);
					codecs[i] = codec;
				}
			} catch (ClassNotFoundException e) {
			}

		}
		CodecRegistry codecRegistry = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(codecs),
				MongoClient.getDefaultCodecRegistry());
		CodecRegistryHolder.getInstance().setCodecRegistry(codecRegistry);
	}
}
