package net.jahhan.mq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import net.jahhan.api.MQReceiver;
import net.jahhan.constant.SysConfiguration;
import net.jahhan.constant.SystemErrorCode;
import net.jahhan.constant.enumeration.DBConnectionType;
import net.jahhan.constant.enumeration.ThreadPoolEnum;
import net.jahhan.context.BaseContext;
import net.jahhan.context.InvocationContext;
import net.jahhan.db.conn.DBConnFactory;
import net.jahhan.db.dbconnexecutor.DBConnExecutorFactory;
import net.jahhan.exception.FrameworkException;
import net.jahhan.factory.ThreadPoolFactory;
import net.jahhan.mq.annotation.MqListener;
import net.jahhan.spi.SerializerHandler;
import net.jahhan.utils.ClassScaner;
import net.jahhan.utils.JsonUtil;

public abstract class MqScaner {
	protected Logger logger = LoggerFactory.getLogger(MqScaner.class);
	private static final String[] packages = new String[] { SysConfiguration.getCompanyName() + ".mq.listener",
			"net.jahhan.mq.listener" };
	protected Map<String, Set<ReceiverHolder>> pubSublisteners = new HashMap<>();
	protected Map<String, Set<ReceiverHolder>> proConlisteners = new HashMap<>();
	protected Map<String, Set<ReceiverHolder>> safeProConlisteners = new HashMap<>();
	@Inject
	private Injector injector;
	@Inject
	private ThreadPoolFactory threadPoolFactory;
	@Inject
	@Named("java")
	private SerializerHandler serializer;

	public void scan() {
		List<String> classNameList = new ClassScaner().parse(packages);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		for (String className : classNameList) {
			try {
				Class<?> clazz = classLoader.loadClass(className);
				if (clazz.isAnnotationPresent(MqListener.class) && MQReceiver.class.isAssignableFrom(clazz)) {
					MqListener mqListener = clazz.getAnnotation(MqListener.class);
					MQReceiver mQReceiver = (MQReceiver) injector.getInstance(clazz);
					if (mqListener.mqMode().equals(MqMode.PubSub)) {
						Set<ReceiverHolder> receiverHolders = pubSublisteners
								.get(mqListener.topicType().getParentType() + mqListener.topicName());
						if (null == receiverHolders) {
							receiverHolders = new HashSet<ReceiverHolder>();
							pubSublisteners.put(mqListener.topicType().getParentType() + mqListener.topicName(),
									receiverHolders);
						}
						ReceiverHolder receiverHolder = new ReceiverHolder(mQReceiver, mqListener.conn(),
								mqListener.mqMode());
						receiverHolders.add(receiverHolder);
					} else if (mqListener.mqMode().equals(MqMode.ProCon)) {
						Set<ReceiverHolder> receiverHolders = proConlisteners
								.get(mqListener.topicType().getParentType() + mqListener.topicName());
						if (null == receiverHolders) {
							receiverHolders = new HashSet<ReceiverHolder>();
							proConlisteners.put(mqListener.topicType().getParentType() + mqListener.topicName(),
									receiverHolders);
						}
						ReceiverHolder receiverHolder = new ReceiverHolder(mQReceiver, mqListener.conn(),
								mqListener.mqMode());
						receiverHolders.add(receiverHolder);
					} else if (mqListener.mqMode().equals(MqMode.SafeProCon)) {
						Set<ReceiverHolder> receiverHolders = safeProConlisteners
								.get(mqListener.topicType().getParentType() + mqListener.topicName());
						if (null == receiverHolders) {
							receiverHolders = new HashSet<ReceiverHolder>();
							safeProConlisteners.put(mqListener.topicType().getParentType() + mqListener.topicName(),
									receiverHolders);
						}
						ReceiverHolder receiverHolder = new ReceiverHolder(mQReceiver, mqListener.conn(),
								mqListener.mqMode());
						receiverHolders.add(receiverHolder);
					}
				}
			} catch (Exception e) {
				FrameworkException.throwException(SystemErrorCode.CODE_ERROR, className + "监听器构造器错误");
			}
		}
		register();
	}

	protected void pubSubMsgHandle(String channel, String msg) {
		Set<ReceiverHolder> receiverHolders = pubSublisteners.get(channel);
		if (null != receiverHolders && receiverHolders.size() > 0) {
			Message message = JsonUtil.fromJson(msg, Message.class);
			logger.debug("监听到话题：" + channel + "->消息id：" + message.getId());
			for (ReceiverHolder receiverHolder : receiverHolders) {
				MsgSplitor msgSplitor = new MsgSplitor(receiverHolder.getmQReceiver(), message,
						receiverHolder.getdBConnectionType());
				Thread t = new Thread(msgSplitor);
				ExecutorService executorService = threadPoolFactory.getExecuteService(ThreadPoolEnum.FIXED);
				executorService.execute(t);
			}
		}
	}

	protected void proConMsgHandle(String channel, byte[] msg) {
		Set<ReceiverHolder> receiverHolders = proConlisteners.get(channel);
		if (null != receiverHolders && receiverHolders.size() > 0) {
			Message message = (Message) serializer.deserializeInto(msg);
			logger.debug("监听到话题：" + channel + "->消息id：" + message.getId());
			for (ReceiverHolder receiverHolder : receiverHolders) {
				MsgSplitor msgSplitor = new MsgSplitor(receiverHolder.getmQReceiver(), message,
						receiverHolder.getdBConnectionType());
				Thread t = new Thread(msgSplitor);
				ExecutorService executorService = threadPoolFactory.getExecuteService(ThreadPoolEnum.FIXED);
				executorService.execute(t);
			}
		}
	}

	protected void safeProConMsgHandle(String channel, byte[] msg) {
		Set<ReceiverHolder> receiverHolders = safeProConlisteners.get(channel);
		if (null != receiverHolders && receiverHolders.size() > 0) {
			Message message = (Message) serializer.deserializeInto(msg);
			logger.debug("监听到话题：" + channel + "->消息id：" + message.getId());
			for (ReceiverHolder receiverHolder : receiverHolders) {
				MsgSplitor msgSplitor = new MsgSplitor(receiverHolder.getmQReceiver(), message,
						receiverHolder.getdBConnectionType());
				Thread t = new Thread(msgSplitor);
				ExecutorService executorService = threadPoolFactory.getExecuteService(ThreadPoolEnum.FIXED);
				executorService.execute(t);
			}
		}
	}

	public void commit(Long msgId) {

	}

	protected abstract void register();


	public class MsgSplitor implements Runnable {
		private MQReceiver mQReceiver;
		private Message message;
		private DBConnectionType dBConnectionType;

		public MsgSplitor(MQReceiver mQReceiver, Message message, DBConnectionType dBConnectionType) {
			this.mQReceiver = mQReceiver;
			this.message = message;
			this.dBConnectionType = dBConnectionType;
		}

		public void run() {
			BaseContext applicationContext = BaseContext.CTX;
			InvocationContext invocationContext = new InvocationContext(null, null);
			applicationContext.getThreadLocalUtil().openThreadLocal(invocationContext);
			invocationContext.setConnectionType(dBConnectionType);
			DBConnExecutorFactory connExec = new DBConnExecutorFactory(dBConnectionType);
			try {
				connExec.beginConnection();
				mQReceiver.listen(message);
				connExec.endConnection();
				commit(message.getId());
			} catch (Exception e) {
				logger.error("DBConnHandler error {}", e);
				connExec.rollback();
			} catch (Error e) {
				logger.error("DBConnHandler error {}", e);
				connExec.rollback();
			} finally {
				connExec.close();
				DBConnFactory.freeConns(invocationContext.getConnections());
			}
		}
	}

	public class ReceiverHolder {
		private MQReceiver mQReceiver;
		private DBConnectionType dBConnectionType;
		private MqMode mqMode;

		public ReceiverHolder(MQReceiver mQReceiver, DBConnectionType dBConnectionType, MqMode mqMode) {
			super();
			this.mQReceiver = mQReceiver;
			this.dBConnectionType = dBConnectionType;
			this.mqMode = mqMode;
		}

		public MQReceiver getmQReceiver() {
			return mQReceiver;
		}

		public DBConnectionType getdBConnectionType() {
			return dBConnectionType;
		}

		public MqMode getMqMode() {
			return mqMode;
		}

	}
}
