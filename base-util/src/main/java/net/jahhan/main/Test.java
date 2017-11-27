package net.jahhan.main;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.spi.SerializerHandler;

public class Test {
	protected Logger logger = LoggerFactory.getLogger(Test.class);
	@Inject
	@Named("java")
	private SerializerHandler serializer;
	@Inject
	private SerializerHandler serializer2;

	public void scan() {
		logger.debug(serializer + "");
		logger.debug(serializer2 + "");
	}

}
