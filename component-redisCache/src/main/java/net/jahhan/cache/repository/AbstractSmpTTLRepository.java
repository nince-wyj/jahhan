package net.jahhan.cache.repository;

import java.util.EventObject;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisConfigurationManger;
import net.jahhan.cache.RedisFactory;
import net.jahhan.db.event.DBEvent;

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
    protected int getExistSecond() {
        return RedisConfigurationManger.getGlobalExpireSecond();
    }

    public void set(String id, String json) {
        Redis redis = RedisFactory.getMainRedis(getType(), id);
        String key = getKey(id);
        redis.setEx(key, getExistSecond(), json);
    }

    /**
     * 当映射次数达到当前次数后，就将缓存过期掉
     * 
     * @return
     */
    protected int getMaxGetCount() {
        return 5000;
    }

//    @Override
//    public String get(String id) {
//        return super.get(id);
//    }

}
