package net.jahhan.db;

import java.util.EventObject;

import net.jahhan.demand.DBEventListener;

public interface ListenerGroupHandler {

    /*
     * 表示该group是否接受当前类型的event。一般是用event的class类型来判断的
     * @return
     */
    // boolean accept(EventObject event);
    /**
     * 如果这个组的listen和support的listen的类型一致，就返回true。否则返回false。 注意：即使返回true，也不代表就添加到group中，因为如果重复的话，也照样没有添加进来
     * 
     * @param listner
     * @return
     */
    boolean addListener(DBEventListener listener);

    DBEventListener removeListener(DBEventListener listener);

    void listen(EventObject event);

}