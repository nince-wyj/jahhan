package net.jahhan.db;

import java.sql.Connection;

import net.jahhan.db.event.DBEvent;
import net.jahhan.db.event.EventOperate;

public interface PublisherHandler {

    /**
     * 已经被改变的事件，适用于自动commit的连接
     * 
     * @param event
     */
    void realPublishWrite(DBEvent event);

    /**
     * 发布对象被修改的事件
     * 
     * @see EventOperate
     */
    void publishWrite(Connection conn, DBEvent event);

    /**
     * 读取
     * 
     * @param event
     */
    void publishRead(DBEvent event);
}
