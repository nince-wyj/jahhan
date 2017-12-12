package net.jahhan.cache;


import java.util.Map;

/**
 * Created by linwb on 2017/12/6 0006.
 */
public interface CustomCacheKeyCreater {
    /**
     * 创建缓存key
     * @param attachment 调用上下文
     * @param arguments 參數
     * @return
     */
    public String createCacheKey(Map<String,String> attachment,Object[] arguments);
}
