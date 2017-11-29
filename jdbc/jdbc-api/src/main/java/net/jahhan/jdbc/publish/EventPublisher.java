package net.jahhan.jdbc.publish;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.jdbc.ListenerGroupHandler;
import net.jahhan.jdbc.event.DBEvent;
import net.jahhan.jdbc.listenergroup.GenericListenerGroup;
import net.jahhan.spi.DBEventListener;

/**
 * 监听器对外提供的接口
 * 
 * @author nince
 */
public class EventPublisher {
    protected static Logger logger = LoggerFactory.getLogger("event");

    private static Map<String, ListenerGroupHandler> listenerGroups = new HashMap<String, ListenerGroupHandler>();

    private static ListenerGroupHandler commonGroup = new GenericListenerGroup();

    /**
     * 发布事件
     * 
     * @param event
     */
    public static void publish(DBEvent event) {
        commonGroup.listen(event);
        String type = event.getType();
        ListenerGroupHandler group = listenerGroups.get(type);
        if (group == null) {
            logger.debug("{}没有监听类", type);
            return;
        }
        group.listen(event);
    }

    /**
     * 添加监听器
     * 
     * @param listener
     * @return
     */
    public synchronized static boolean addListener(DBEventListener listener) {
        String[] tags = listener.getTags();
        if (tags == null) {
            if (commonGroup.addListener(listener)) {
                logger.info("{}添加到commonGroup中", listener);
                return true;
            }
            return false;
        }
        for (String tag : tags) {
            ListenerGroupHandler group = listenerGroups.get(tag);
            if (group == null) {
                group = new GenericListenerGroup();
                listenerGroups.put(tag, group);
            }
            group.addListener(listener);
        }
        return true;
    }

    /**
     * 移除监听器.如果监听器不存在，就返回null
     * 
     * @return
     */
    public synchronized static void removeListener(DBEventListener listener) {
        String[] tags = listener.getTags();
        if (tags == null) {
            commonGroup.removeListener(listener);
            return;
        }
        for (String tag : tags) {
            ListenerGroupHandler group = listenerGroups.get(tag);
            if (group == null) {
                continue;
            }
            group.removeListener(listener);
        }
    }

}