package net.jahhan.jdbc.publish;

import net.jahhan.jdbc.PublisherHandler;
import net.jahhan.jdbc.event.DBEvent;

public abstract class AbstractPublisherHandler implements PublisherHandler {

    public void publishRead(DBEvent event) {
        EventPublisherManager.publish(event);
    }

    public void realPublishWrite(DBEvent event) {
        EventPublisherManager.publish(event);
    }
}
