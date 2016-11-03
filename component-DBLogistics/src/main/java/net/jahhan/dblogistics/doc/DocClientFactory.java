package net.jahhan.dblogistics.doc;

import com.mongodb.MongoClient;

public class DocClientFactory {
	private static DocClientFactory instance = new DocClientFactory();

	public static DocClientFactory getInstance() {
		return instance;
	}

	MongoClient client;

	public MongoClient getClient() {
		return client;
	}

	public void setClient(MongoClient client) {
		this.client = client;
	}

}
