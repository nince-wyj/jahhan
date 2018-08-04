package net.jahhan.extension.protocol;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.inject.Singleton;

import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.ipc.reflect.ReflectResponder;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.common.extension.exception.JahhanException;

/**
 * 为dubbo-rpc添加avro支持
 * by 杨俊明(http://yjmyzz.cnblogs.com/)
 */
@Extension("avro")
@Singleton
@Slf4j
public class AvroProtocol extends AbstractProxyProtocol {
    public static final int DEFAULT_PORT = 40881;

    public AvroProtocol() {
        super(IOException.class, JahhanException.class);
    }

    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    protected <T> Runnable doExport(T impl, Class<T> type, URL url)
            throws JahhanException {

        log.info("impl => " + impl.getClass());
        log.info("type => " + type.getName());
        log.info("url => " + url);

        final Server server = new NettyServer(new ReflectResponder(type, impl),
                new InetSocketAddress(url.getHost(), url.getPort()));
        server.start();

        log.info("Start Avro Server");

        return new Runnable() {
            public void run() {
                try {
                    log.info("Close Avro Server");
                    server.close();
                } catch (Throwable e) {
                    log.warn(e.getMessage(), e);
                }
            }
        };
    }

    @Override
    protected <T> T doRefer(Class<T> type, URL url) throws JahhanException {

        log.info("type => " + type.getName());
        log.info("url => " + url);

        try {
            NettyTransceiver client = new NettyTransceiver(new InetSocketAddress(url.getHost(), url.getPort()));
            T ref = ReflectRequestor.getClient(type, client);
            log.info("Create Avro Client");
            return ref;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JahhanException("Fail to create remoting client for service(" + url + "): " + e.getMessage(), e);
        }
    }

}
