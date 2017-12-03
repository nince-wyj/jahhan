package com.frameworkx.rpc.grpc;

import io.grpc.ManagedChannel;
import lombok.Data;

@Data
public abstract class GrpcAbstractInvoker {
	protected ManagedChannel channel;
}
