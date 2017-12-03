package net.jahhan.extension.protocol;

import java.lang.reflect.Constructor;

import javax.inject.Singleton;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.common.extension.annotation.Extension;
import net.jahhan.exception.JahhanException;

/**
 * 为dubbo-rpc添加"原生thrift"支持
 * by 杨俊明(http://yjmyzz.cnblogs.com/)
 */
@Extension("thrift")
@Singleton
@Slf4j
public class ThriftProtocol extends AbstractProxyProtocol {
    public static final int DEFAULT_PORT = 33208;

    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    public ThriftProtocol() {
        super(TException.class, JahhanException.class);
    }


    @Override
    protected <T> Runnable doExport(T impl, Class<T> type, URL url)
            throws JahhanException {
        log.info("impl => " + impl.getClass());
        log.info("type => " + type.getName());
        log.info("url => " + url);
        return exportThreadedSelectorServer(impl, type, url);
    }

    @Override
    protected <T> T doRefer(Class<T> type, URL url) throws JahhanException {
        log.info("type => " + type.getName());
        log.info("url => " + url);
        return doReferFrameAndCompact(type, url);
    }


    private <T> Runnable exportThreadedSelectorServer(T impl, Class<T> type, URL url)
            throws JahhanException {
        TProcessor tprocessor;
        TThreadedSelectorServer.Args tArgs = null;
        String iFace = "$Iface";
        String processor = "$Processor";
        String typeName = type.getName();
        TNonblockingServerSocket transport;
        if (typeName.endsWith(iFace)) {
            String processorClsName = typeName.substring(0, typeName.indexOf(iFace)) + processor;
            try {
                Class<?> clazz = Class.forName(processorClsName);
                Constructor constructor = clazz.getConstructor(type);
                try {
                    tprocessor = (TProcessor) constructor.newInstance(impl);

                    //解决并发连接数上限默认只有50的问题
                    TNonblockingServerSocket.NonblockingAbstractServerSocketArgs args = new TNonblockingServerSocket.NonblockingAbstractServerSocketArgs();
                    args.backlog(1000);//1k个连接
                    args.port(url.getPort());
                    args.clientTimeout(10000);//10秒超时

                    transport = new TNonblockingServerSocket(args);

                    tArgs = new TThreadedSelectorServer.Args(transport);
                    tArgs.workerThreads(200);
                    tArgs.selectorThreads(4);
                    tArgs.acceptQueueSizePerThread(256);
                    tArgs.processor(tprocessor);
                    tArgs.transportFactory(new TFramedTransport.Factory());
                    tArgs.protocolFactory(new TCompactProtocol.Factory());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new JahhanException("Fail to create thrift server(" + url + ") : " + e.getMessage(), e);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new JahhanException("Fail to create thrift server(" + url + ") : " + e.getMessage(), e);
            }
        }

        if (tArgs == null) {
            log.error("Fail to create thrift server(" + url + ") due to null args");
            throw new JahhanException("Fail to create thrift server(" + url + ") due to null args");
        }
        final TServer thriftServer = new TThreadedSelectorServer(tArgs);

        new Thread(new Runnable() {
            public void run() {
                log.info("Start Thrift ThreadedSelectorServer");
                thriftServer.serve();
                log.info("Thrift ThreadedSelectorServer started.");
            }
        }).start();

        return new Runnable() {
            public void run() {
                try {
                    log.info("Close Thrift NonblockingServer");
                    thriftServer.stop();
                } catch (Throwable e) {
                    log.warn(e.getMessage(), e);
                }
            }
        };
    }

    private <T> T doReferFrameAndCompact(Class<T> type, URL url) throws JahhanException {

        try {
            TSocket tSocket;
            TTransport transport;
            TProtocol protocol;
            T thriftClient = null;
            String iFace = "$Iface";
            String client = "$Client";

            String typeName = type.getName();
            if (typeName.endsWith(iFace)) {
                String clientClsName = typeName.substring(0, typeName.indexOf(iFace)) + client;
                Class<?> clazz = Class.forName(clientClsName);
                Constructor constructor = clazz.getConstructor(TProtocol.class);
                try {
                    tSocket = new TSocket(url.getHost(), url.getPort());
                    transport = new TFramedTransport(tSocket);
                    protocol = new TCompactProtocol(transport);
                    thriftClient = (T) constructor.newInstance(protocol);
                    transport.open();
                    log.info("thrift client opened for service(" + url + ")");
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new JahhanException("Fail to create remote client:" + e.getMessage(), e);
                }
            }
            return thriftClient;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JahhanException("Fail to create remote client for service(" + url + "): " + e.getMessage(), e);
        }
    }

}
