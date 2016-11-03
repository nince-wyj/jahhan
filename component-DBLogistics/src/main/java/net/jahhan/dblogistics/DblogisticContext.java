package net.jahhan.dblogistics;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.db.event.CollectionEvent;
import net.jahhan.db.event.CountEvent;
import net.jahhan.db.event.DBEvent;
import net.jahhan.db.event.DataModifyEvent;
import net.jahhan.db.event.EventOperate;
import net.jahhan.db.event.ListEvent;
import net.jahhan.db.event.NumModifyEvent;
import net.jahhan.db.publish.EventPublisherManager;
import net.jahhan.dblogistics.publish.DBPublisherHandler;
import net.jahhan.utils.TagUtil;

public class DblogisticContext implements PublisherHandler {

	private PublisherHandler publisher;

	private Logger logger = LoggerFactory.getLogger(DblogisticContext.class);

	private DblogisticContext() {
		publisher = DBPublisherHandler.getPublisher();
	}

	private static DblogisticContext appContext = new DblogisticContext();

	public static DblogisticContext instance() {
		return appContext;
	}

	public static void setPublisher(PublisherHandler publisher) {
		appContext.publisher = publisher;
	}

	public void publishWrite(DBEvent event) {
		publisher.publishWrite(event);
	}

	public void publishDataModifyEvent(Object pojo, String operate, String id) {
		if (pojo == null || operate == null) {
			logger.error("");
			return;
		}
		String type = TagUtil.getType(pojo.getClass());
		DataModifyEvent event = new DataModifyEvent(pojo, type, operate, id);
		publishWrite(event);
	}

	public void publishDeleteEvent(Object pojo, String id) {
		if (pojo == null) {
			logger.error("");
			return;
		}
		String type = TagUtil.getType(pojo.getClass());
		DataModifyEvent event = new DataModifyEvent(pojo, type, EventOperate.DELETE, id);
		publishWrite(event);
	}

	public void publishAddNumEvent(Object pojo, String id, String field, Number num) {
		if (pojo == null || field == null || "".equals(field) || num == null) {
			logger.error("");
			return;
		}
		String type = TagUtil.getType(pojo.getClass());
		NumModifyEvent event = NumModifyEvent.createAdd(type, id, field, num);
		publishWrite(event);
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
		DataModifyEvent event = new DataModifyEvent(pojo, TagUtil.getType(pojo.getClass()), EventOperate.GET, String.valueOf(id));
		publishRead(event);
	}
}
