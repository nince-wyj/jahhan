package net.jahhan.db.publish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.db.PublisherHandler;
import net.jahhan.db.event.DBEvent;

public abstract class AbstractPublisherHandler implements PublisherHandler {
    protected Logger logger = LoggerFactory.getLogger(AbstractPublisherHandler.class);

    public void publishRead(DBEvent event) {
        EventPublisherManager.publish(event);
    }

    public void realPublishWrite(DBEvent event) {
        EventPublisherManager.publish(event);
    }
}
