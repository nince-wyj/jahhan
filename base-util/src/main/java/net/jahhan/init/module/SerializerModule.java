package net.jahhan.init.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import net.jahhan.handler.SerializerHandler;
import net.jahhan.handler.serializer.HessianSerializer;
import net.jahhan.handler.serializer.JavaSerializer;
import net.jahhan.init.InitAnnocation;

@InitAnnocation(isLazy = false, initSequence = 1000)
public class SerializerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(SerializerHandler.class).annotatedWith(Names.named("hessian")).to(HessianSerializer.class);
		bind(SerializerHandler.class).annotatedWith(Names.named("java")).to(JavaSerializer.class);
	}
}
