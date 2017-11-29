package net.jahhan.cache.repository.common;

import java.util.EventObject;

import org.apache.commons.lang3.StringUtils;

import net.jahhan.cache.util.Counter;
import net.jahhan.cache.util.SerializerUtil;
import net.jahhan.jdbc.event.DBEvent;
import net.jahhan.jdbc.event.EventOperate;

/**
 * @author nince
 */
public abstract class AbstractSmpTTLCountRepository extends AbstractSimpleRepository {

	protected Counter counter = new Counter(500);

	public boolean accept(EventObject event) {
		return DBEvent.class.isInstance(event);
	}

	public void set(String id, Object object) {
		String key = getKey(id);
		cache.setEx(key.getBytes(), getExistSecond(), SerializerUtil.serializeFrom(object));
	}

	public void expire(String id) {
		String key = getKey(id);
		cache.expire(key, getExistSecond());
	}

	public Counter getCounter() {
		return counter;
	}

	public <T> T get(String id, Class<T> clazz) {
		if (counter.isCacheRefresh()) {
			return null;
		}
		byte[] bytes = super.getBytes(String.valueOf(id));
		if (bytes != null) {
			counter.incCached();
			return SerializerUtil.deserialize(bytes, clazz);
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
			Object cache = get(id, checkClass);
			if (null == cache) {
				set(id, event.getSource());
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
