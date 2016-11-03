package net.jahhan.cache.repository;

import java.util.Collection;
import java.util.Set;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;

/**
 * set类型的redis
 * 
 * @author nince
 */
public abstract class AbstractSetRepository {

    /**
     * 获取redis对应的key，一般是在id前面加个前缀
     * 
     * @param k
     *            记录的id
     * @return
     */
    protected abstract String getKey(String id);

    /**
     * 获取本Repository要操作的表名称
     * 
     * @return
     */
    protected abstract String getType();

    /**
     * 获取redis中id的set列表
     * 
     * @param id
     * @return
     */
    public Set<String> get(String id) {
        Redis redis = RedisFactory.getReadRedis(getType(), id);
        String key = getKey(id);
        return redis.smembers(key);
    }

    public boolean addAll(String id, Collection<String> set) {
        if (set == null || set.isEmpty()) {
            return false;
        }
        Redis redis = RedisFactory.getMainRedis(getType(), id);
        String key = getKey(id);
        String[] members = set.toArray(new String[set.size()]);
        Long l = redis.sSet(key, members);
        return l != null && l.longValue() == 1;
    }

    /**
     * 往set类型的数据插入一条新数据
     * 
     * @param id
     * @param s
     * @return true表示添加成功，false表示该值已经存在
     */
    public boolean add(String id, String s) {
        Redis redis = RedisFactory.getMainRedis(getType(), id);
        String key = getKey(id);
        Long l = redis.sadd(key, s);
        return l != null && l.longValue() == 1;
    }

    public void del(String id) {
        Redis redis = RedisFactory.getMainRedis(getType(), id);
        String key = getKey(id);
        redis.del(key);
    }

}
