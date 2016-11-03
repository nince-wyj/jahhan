package net.jahhan.dblogistics.doc;

import org.bson.codecs.configuration.CodecRegistry;

public class CodecRegistryHolder {

	private static CodecRegistryHolder instance = new CodecRegistryHolder();

	public static CodecRegistryHolder getInstance() {
		return instance;
	}

	CodecRegistry codecRegistry;

	public CodecRegistry getCodecRegistry() {
		return codecRegistry;
	}

	public void setCodecRegistry(CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
}
