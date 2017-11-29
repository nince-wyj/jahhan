package net.jahhan.cache.repository.common;

import java.util.EventObject;

import net.jahhan.cache.util.DateTimeUtils;
import net.jahhan.jdbc.event.DBEvent;

/**
 * 简单类型，当天晚上24点过期
 * 
 * @author nince
 */
public abstract class AbstractSmpDayRepository extends AbstractSimpleRepository {

	public boolean accept(EventObject event) {
		return DBEvent.class.isInstance(event);
	}

	public void set(String id, String json) {
		String key = getKey(id);
		long night = DateTimeUtils.getTimesNight();
		int sec = (int) ((night - System.currentTimeMillis()) / 1000);
		if (sec < 1) {
			sec = 1;
		}
		cache.setEx(key, sec, json);
	}

	@Override
	public String get(String id) {
		return super.get(id);
	}

}
