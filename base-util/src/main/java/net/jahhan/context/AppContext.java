package net.jahhan.context;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.db.PublisherHandler;
import net.jahhan.db.SessionHandler;
import net.jahhan.db.event.CollectionEvent;
import net.jahhan.db.event.CountEvent;
import net.jahhan.db.event.DBEvent;
import net.jahhan.db.event.DataModifyEvent;
import net.jahhan.db.event.EventOperate;
import net.jahhan.db.event.ListEvent;
import net.jahhan.db.event.NumModifyEvent;
import net.jahhan.db.publish.EventPublisherManager;
import net.jahhan.utils.TagUtil;

public class AppContext implements SessionHandler, PublisherHandler {
    private SessionHandler sessionManager;

    private PublisherHandler publisher;

    private Logger logger = LoggerFactory.getLogger(AppContext.class);

    private AppContext() {

    }

    private static AppContext appContext = new AppContext();

    public static AppContext instance() {
        return appContext;
    }

    public static void setPublisher(PublisherHandler publisher) {
        appContext.publisher = publisher;
    }

    public static void setSessionManager(SessionHandler sessionManager) {
        appContext.sessionManager = sessionManager;
    }

    public SqlSession getMainSession() {
        return sessionManager.getMainSession();
    }
    
    public SqlSession getBatchSession() {
        return sessionManager.getBatchSession();
    }

    public SqlSession getMainReadSession() {
        return sessionManager.getMainReadSession();
    }

    public void publishWrite(Connection conn, DBEvent event) {
      publisher.publishWrite(conn, event);
    }

    public void publishDataModifyEvent(Connection dbConn, Object pojo, String operate, String id) {
        if (pojo == null || operate == null) {
            logger.error("");
            return;
        }
        String type = TagUtil.getType(pojo.getClass());
        if (type.startsWith("Para")) {
            type = type.substring(4);
        } else if (type.endsWith("Page")) {
            type = type.substring(0, type.length() - 4);
        }
        DataModifyEvent event = new DataModifyEvent(pojo, type, operate, id);
        publishWrite(dbConn, event);
    }

    public void publishDeleteEvent(Connection dbConn, Object pojo, String id) {
        if (pojo == null) {
            logger.error("");
            return;
        }
        String type = TagUtil.getType(pojo.getClass());
        if (type.endsWith("Page")) {
            type = type.substring(0, type.length() - 4);
        }
        DataModifyEvent event = new DataModifyEvent(pojo, type, EventOperate.DELETE, id);
        publishWrite(dbConn, event);
    }

    public void publishAddNumEvent(Connection dbConn, Object pojo, String id, String field, Number num) {
        if (pojo == null || field == null || "".equals(field) || num == null) {
            logger.error("");
            return;
        }
        String type = TagUtil.getType(pojo.getClass());
        if (type.startsWith("Para")) {
            type = type.substring(4);
        }
        NumModifyEvent event = NumModifyEvent.createAdd(type, id, field, num);
        publishWrite(dbConn, event);
    }

    public void publishRead(DBEvent event) {
       publisher.publishRead(event);
    }

    public void realPublishWrite(DBEvent event) {
        publisher.realPublishWrite(event);
    }

    public void publishReadListByForeKey(Collection<?> list, Class<?> clz, String foreignId) {
        if (list == null || list.isEmpty() || clz == null || foreignId == null || "".equals(foreignId)) {
            return;
        }
        ListEvent ev = new ListEvent(list, TagUtil.getType(clz), foreignId);
        this.publisher.publishRead(ev);
    }

    public void publishReadList(Class<?> clz, Object para, Collection<?> collection) {
       CollectionEvent event = new CollectionEvent(collection, para, TagUtil.getType(clz));
        EventPublisherManager.publish(event);
    }

    public void publishReadCount(Class<?> clz, Object para, long count) {
       CountEvent event = new CountEvent(Long.valueOf(count), TagUtil.getType(clz), para);
        EventPublisherManager.publish(event);
    }

    public void publishReadPojo(Object pojo, String id) {
       if (pojo == null || id == null) {
            logger.error("");
            return;
        }
        DataModifyEvent event = new DataModifyEvent(pojo, TagUtil.getType(pojo.getClass()), EventOperate.GET, id);
        publishRead(event);
    }

//    @Override
//    public SqlSession getHisSession() {
//        return sessionManager.getHisSession();
//    }

//    @Override
//    public SqlSession getFlowSession() {
//        return sessionManager.getFlowSession();
//    }
//
//    @Override
//    public SqlSession getFlowReadSession() {
//        return sessionManager.getFlowReadSession();
//    }

    @Override
    public Connection getConnection() throws SQLException {
        return sessionManager.getConnection();
    }
}
