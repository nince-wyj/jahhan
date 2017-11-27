package net.jahhan.cache.repository;

import java.util.EventObject;

import org.apache.commons.lang.StringUtils;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;
import net.jahhan.cache.util.Counter;
import net.jahhan.context.BaseContext;
import net.jahhan.db.event.DBEvent;
import net.jahhan.db.event.EventOperate;
import net.jahhan.spi.SerializerHandler;

/**
 * @author nince
 */
public abstract class AbstractSmpTTLCountRepository extends AbstractSimpleRepository {

	protected Counter counter = new Counter(500);

	public boolean accept(EventObject event) {
		return DBEvent.class.isInstance(event);
	}

	public void set(String id, Object object) {
		SerializerHandler serializer = BaseContext.CTX.getSerializer();
		Redis redis = RedisFactory.getMainRedis(getType(), id);
		String key = getKey(id);
		redis.setEx(key.getBytes(), getExistSecond(), serializer.serializeFrom(object));
	}

	public void expire(String id) {
		Redis redis = RedisFactory.getMainRedis(getType(), id);
		String key = getKey(id);
		redis.expire(key, getExistSecond());
	}

	public Counter getCounter() {
		return counter;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String id, Class<T> clazz) {
		if (counter.isCacheRefresh()) {
			return null;
		}
		byte[] bytes = super.getBytes(String.valueOf(id));
		if (bytes != null) {
			counter.incCached();
			SerializerHandler serializer = BaseContext.CTX.getSerializer();
			return (T) serializer.deserializeInto(bytes);
		}
		return null;
	}

	/**
	 * 默认的监听行为
	 * 
	 * @param event
	 * @param checkClass
	 *            用于校验对象的类型
	 * @author nince
	 */
	protected void onListen(EventObject event, Class<?> checkClass) {
		DBEvent ev = (DBEvent) event;
		String id = ev.getId();
		String op = ev.getOperate();
		if (StringUtils.isEmpty(id)) {
			return;
		}
		if (op.equals(EventOperate.GET)) {
			if (!checkClass.isInstance(event.getSource())) {
				return;
			}
			expire(id);
		} else if (op.equals(EventOperate.INSERT) || op.equals(EventOperate.UPDATE)) {
			if (!checkClass.isInstance(event.getSource())) {
				return;
			}
			set(id, event.getSource());
		} else if (op.equals(EventOperate.PART_MODIFY)) {
			merge(id, ev.getSource(), checkClass);
		} else if (EventOperate.isModify(op)) {
			del(id);
		}
	}

}
