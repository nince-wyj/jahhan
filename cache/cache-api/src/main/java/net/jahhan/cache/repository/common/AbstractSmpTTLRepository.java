package net.jahhan.cache.repository.common;

import java.util.EventObject;

import net.jahhan.common.extension.constant.BaseConfiguration;
import net.jahhan.jdbc.event.DBEvent;

/**
 * 简单类型，并且有过期时间
 * 
 * @author nince
 */
public abstract class AbstractSmpTTLRepository extends AbstractSimpleRepository {

	public boolean accept(EventObject event) {
		return DBEvent.class.isInstance(event);
	}

	/**
	 * 在redis中存活的时间
	 * 
	 * @return
	 */
	public int getExistSecond() {
		return BaseConfiguration.GLOBAL_EXPIRE_SECOND;
	}

	public void set(String id, String json) {
		String key = getKey(id);
		cache.setEx(key, getExistSecond(), json);
	}

	/**
	 * 当映射次数达到当前次数后，就将缓存过期掉
	 * 
	 * @return
	 */
	protected int getMaxGetCount() {
		return 5000;
	}

	@Override
	public String get(String id) {
		return super.get(id);
	}

}
