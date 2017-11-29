package net.jahhan.jdbc.context;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.ibatis.session.SqlSession;

import lombok.extern.slf4j.Slf4j;
import net.jahhan.jdbc.PublisherHandler;
import net.jahhan.jdbc.SessionHandler;
import net.jahhan.jdbc.SuperPojo;
import net.jahhan.jdbc.event.CollectionEvent;
import net.jahhan.jdbc.event.CountEvent;
import net.jahhan.jdbc.event.DBEvent;
import net.jahhan.jdbc.event.DataModifyEvent;
import net.jahhan.jdbc.event.EventOperate;
import net.jahhan.jdbc.event.ListEvent;
import net.jahhan.jdbc.event.NumModifyEvent;
import net.jahhan.jdbc.publish.EventPublisherManager;
import net.jahhan.jdbc.utils.TagUtil;

@Slf4j
@Singleton
public class DBContext implements SessionHandler, PublisherHandler {
	@Inject
	private SessionHandler sessionManager;
	@Inject
	private PublisherHandler publisher;

	@Override
	public SqlSession getWriteSession(String dataSource) {
		return sessionManager.getWriteSession(dataSource);
	}

	@Override
	public SqlSession getBatchSession(String dataSource) {
		return sessionManager.getBatchSession(dataSource);
	}

	@Override
	public SqlSession getReadSession(String dataSource) {
		return sessionManager.getReadSession(dataSource);
	}

	public void publishWrite(Connection conn, DBEvent event) {
		publisher.publishWrite(conn, event);
	}

	public void publishDataModifyEvent(String dataSource, Connection dbConn, SuperPojo<?> pojo, String operate, String id) {
		if (pojo == null || operate == null) {
			log.error("");
			return;
		}
		String type = TagUtil.getType(pojo.getClass());
		if (type.startsWith("Para")) {
			type = type.substring(4);
		} else if (type.endsWith("Page")) {
			type = type.substring(0, type.length() - 4);
		}
		DataModifyEvent event = new DataModifyEvent(pojo, dataSource, type, operate, id);
		publishWrite(dbConn, event);
	}

	public void publishDeleteEvent(String dataSource, Connection dbConn, SuperPojo<?> pojo, String id) {
		if (pojo == null) {
			log.error("");
			return;
		}
		String type = TagUtil.getType(pojo.getClass());
		if (type.endsWith("Page")) {
			type = type.substring(0, type.length() - 4);
		}
		DataModifyEvent event = new DataModifyEvent(pojo, dataSource, type, EventOperate.DELETE, id);
		publishWrite(dbConn, event);
	}

	public void publishAddNumEvent(String dataSource, Connection dbConn, SuperPojo<?> pojo, String id, String field,
			Number num) {
		if (pojo == null || field == null || "".equals(field) || num == null) {
			log.error("");
			return;
		}
		String type = TagUtil.getType(pojo.getClass());
		if (type.startsWith("Para")) {
			type = type.substring(4);
		}
		NumModifyEvent event = NumModifyEvent.createAdd(dataSource, type, id, field, num);
		publishWrite(dbConn, event);
	}

	public void publishRead(DBEvent event) {
		publisher.publishRead(event);
	}

	public void realPublishWrite(DBEvent event) {
		publisher.realPublishWrite(event);
	}

	public void publishReadListByForeKey(String dataSource, Collection<?> list, Class<?> clz, String foreignId) {
		if (list == null || list.isEmpty() || clz == null || foreignId == null || "".equals(foreignId)) {
			return;
		}
		ListEvent ev = new ListEvent(list, dataSource, TagUtil.getType(clz), foreignId);
		this.publisher.publishRead(ev);
	}

	public void publishReadList(String dataSource, Class<?> clz, Object para, Collection<?> collection) {
		CollectionEvent event = new CollectionEvent(collection, dataSource, para, TagUtil.getType(clz));
		EventPublisherManager.publish(event);
	}

	public void publishReadCount(String dataSource, Class<?> clz, Object para, long count) {
		CountEvent event = new CountEvent(dataSource, Long.valueOf(count), TagUtil.getType(clz), para);
		EventPublisherManager.publish(event);
	}

	public void publishReadPojo(String dataSource, SuperPojo<?> pojo, String id) {
		if (pojo == null || id == null) {
			log.error("");
			return;
		}
		DataModifyEvent event = new DataModifyEvent(pojo, dataSource, TagUtil.getType(pojo.getClass()),
				EventOperate.GET, id);
		publishRead(event);
	}

	@Override
	public Connection getConnection(String dataSource) throws SQLException {
		return sessionManager.getConnection(dataSource);
	}
}
