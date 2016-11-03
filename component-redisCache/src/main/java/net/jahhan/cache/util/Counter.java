package net.jahhan.cache.util;

public class Counter {
    /*
     * 缓存被访问次数
     */
    private volatile long visitCount = 0;

    /*
     * 缓存命中次数
     */
    private volatile long cacheCount = 0;

    /*
     * 每访问refreshCount次就刷新一次缓存
     */
    private int refreshCount;

    public Counter(int refreshCount) {
        super();
        this.refreshCount = refreshCount;
    }

    public long getVisitCount() {
        return visitCount;
    }

    public long getCachedCount() {
        return cacheCount;
    }

    /**
     * 一定访问次数的时候，返回true。用于刷新缓存
     * 
     * @return true表示不适用缓存
     */
    public boolean isCacheRefresh() {
        return ++visitCount == refreshCount;
    }

    public void incCached() {
        cacheCount++;
    }

    @Override
    public String toString() {
        return "Counter [访问次数=" + visitCount + ", 命中次数=" + cacheCount + "]";
    }

}
