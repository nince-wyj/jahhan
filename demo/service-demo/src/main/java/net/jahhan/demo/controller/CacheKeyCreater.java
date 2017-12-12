package net.jahhan.demo.controller;

import net.jahhan.cache.CustomCacheKeyCreater;

import java.util.Map;

/**
 * Created by linwb on 2017/12/12 0012.
 */
public class CacheKeyCreater implements CustomCacheKeyCreater{
    @Override
    public String createCacheKey(Map<String, String> attachment, Object[] arguments) {
        return "_customKey:(idx[0]:"+arguments[0];
    }
}
