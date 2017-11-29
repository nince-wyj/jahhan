package net.jahhan.cache.repository;

import java.util.Set;

import net.jahhan.cache.Redis;
import net.jahhan.cache.RedisFactory;

/**
 * 有序set类型的redis
 * 
 * @author nince
 */
public abstract class AbstractZSetRepository {
	Redis redis = RedisFactory.getRedis(getType(), null);
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
        
        String key = getKey(id);
        return redis.smembers(key);
    }

    /**
     * 往set类型的数据插入一条新数据
     * 
     * @param id
     * @param s
     * @return true表示添加成功，false表示该值已经存在
     */
    public boolean add(String id, String s, double score) {
        String key = getKey(id);
        Long l = redis.zadd(key, s, score);
        return l != null && l.longValue() == 1;
    }

    public void del(String id) {
        String key = getKey(id);
        redis.del(key);
    }

}
