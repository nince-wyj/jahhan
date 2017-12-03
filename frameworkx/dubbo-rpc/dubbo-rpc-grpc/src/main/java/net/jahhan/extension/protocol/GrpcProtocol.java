package net.jahhan.extension.protocol;

import java.io.IOException;

import javax.inject.Singleton;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import com.frameworkx.rpc.grpc.GrpcAbstractInvoker;
import com.frameworkx.rpc.grpc.GrpcServerHolder;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.context.BaseContext;
import net.jahhan.exception.JahhanException;

@Extension("grpc")
@Singleton
@Slf4j
public class GrpcProtocol extends AbstractProxyProtocol {
	public static final int DEFAULT_PORT = 50051;

	public int getDefaultPort() {
		return DEFAULT_PORT;
	}

	public GrpcProtocol() {
		super(IOException.class, JahhanException.class);
	}

	private static final String packageName = ".soa.service.grpc.";

	@Override
	protected <T> Runnable doExport(T impl, Class<T> type, URL url) throws JahhanException {
		String name = type.getName();
		try {
			String substring = name.substring(name.lastIndexOf(".") + 1,
					name.indexOf(BaseConfiguration.INTERFACE_SUFFIX));
			String grpcImplName;
			if (name.startsWith(BaseConfiguration.COMPANY_PATH)) {
				grpcImplName = BaseConfiguration.COMPANY_PATH + packageName + substring + "GrpcImpl";
			} else if (name.startsWith(BaseConfiguration.FRAMEWORK_PATH)) {
				grpcImplName = BaseConfiguration.FRAMEWORK_PATH + packageName + substring + "GrpcImpl";
			} else {
				grpcImplName = "com" + packageName + substring + "GrpcImpl";
			}

			Class<?> grpcImplClass = Class.forName(grpcImplName);
			BindableService service = (BindableService) BaseContext.CTX.getInjector().getInstance(grpcImplClass);
			ServerBuilder<?> serverBuilder;
			if (!GrpcServerHolder.containsKey(url.getPort())) {
				serverBuilder = ServerBuilder.forPort(url.getPort());
				GrpcServerHolder.putServerBuilder(url.getPort(), serverBuilder);
			} else {
				serverBuilder = GrpcServerHolder.getServerBuilder(url.getPort());
			}
			serverBuilder.addService(service);

			return new Runnable() {
				public void run() {
					try {
						log.info("Close gRPC Server");
						GrpcServerHolder.getServer(url.getPort()).shutdown();
					} catch (Throwable e) {
						log.warn(e.getMessage(), e);
					}
				}
			};
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JahhanException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T doRefer(Class<T> type, URL url) throws JahhanException {
		String name = type.getName();
		final ManagedChannel channel = ManagedChannelBuilder.forAddress(url.getHost(), url.getPort()).usePlaintext(true)
				.build();
		try {
			String substring = name.substring(name.lastIndexOf(".") + 1,
					name.indexOf(BaseConfiguration.INTERFACE_SUFFIX));
			String grpcImplName;
			if (name.startsWith(BaseConfiguration.COMPANY_PATH)) {
				grpcImplName = BaseConfiguration.COMPANY_PATH + packageName + substring + "GrpcInvoker";
			} else if (name.startsWith(BaseConfiguration.FRAMEWORK_PATH)) {
				grpcImplName =BaseConfiguration.FRAMEWORK_PATH + packageName + substring + "GrpcInvoker";
			} else {
				grpcImplName = "com" + packageName + substring + "GrpcInvoker";
			}
			Class<?> grpcImplClass = Class.forName(grpcImplName);
			GrpcAbstractInvoker newInstance = (GrpcAbstractInvoker) BaseContext.CTX.getInjector().getInstance(grpcImplClass);
			newInstance.setChannel(channel);
			return (T) newInstance;
		} catch (Exception e) {
			throw new JahhanException(e.getMessage(), e);
		}
	}
}
