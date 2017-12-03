package com.frameworkx.rpc.grpc;

import java.util.HashMap;
import java.util.Map;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GrpcServerHolder {
	private static Map<Integer, ServerBuilder<?>> serverBuilderMap = new HashMap<>();

	public static void putServerBuilder(Integer port, ServerBuilder<?> serverbuilder) {
		serverBuilderMap.put(port, serverbuilder);
	}

	public static boolean containsKey(Integer port) {
		return serverBuilderMap.containsKey(port);
	}

	public static ServerBuilder<?> getServerBuilder(Integer port) {
		return serverBuilderMap.get(port);
	}
	
	public static Map<Integer, ServerBuilder<?>> getServerBuilderMap(){
		return serverBuilderMap;
	}
	
	
	private static Map<Integer, Server> serverMap = new HashMap<>();

	public static void putServer(Integer port, Server server) {
		serverMap.put(port, server);
	}

	public static boolean containsServer(Integer port) {
		return serverMap.containsKey(port);
	}

	public static Server getServer(Integer port) {
		return serverMap.get(port);
	}
	
	public static Map<Integer, Server> getServerMap(){
		return serverMap;
	}
}
