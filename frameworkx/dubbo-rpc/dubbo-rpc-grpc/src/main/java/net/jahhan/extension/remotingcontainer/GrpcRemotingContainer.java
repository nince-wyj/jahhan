package net.jahhan.extension.remotingcontainer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.frameworkx.rpc.grpc.GrpcServerHolder;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.spi.RemotingContainer;

@Extension("grpc")
public class GrpcRemotingContainer implements RemotingContainer{

	@Override
	public void start() {
		Map<Integer, ServerBuilder<?>> serverBuilderMap = GrpcServerHolder.getServerBuilderMap();
		Set<Integer> portSet = serverBuilderMap.keySet();
		for(Integer port:portSet){
			ServerBuilder<?> serverBuilder = serverBuilderMap.get(port);
			try {
				Server server = serverBuilder.build().start();
				GrpcServerHolder.putServer(port, server);
			} catch (IOException e) {
			}
		}
	}

}
