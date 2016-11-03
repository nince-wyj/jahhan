package net.jahhan.db.listenergroup;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jahhan.db.ListenerGroupHandler;
import net.jahhan.demand.DBEventListener;

public class GenericListenerGroup implements ListenerGroupHandler {

    private List<DBEventListener> listeners = new ArrayList<DBEventListener>();

    protected Logger logger = LoggerFactory.getLogger("event.listenerGroup");

    private static final Object lock = new Object();

    @Override
    public DBEventListener removeListener(DBEventListener listener) {
        for (int i = listeners.size() - 1; i >= 0; i--) {
            if (listeners.get(i).equals(listener)) {
                synchronized (lock) {
                    return listeners.remove(i);
                }
            }
        }
        return null;
    }

    @Override
    public boolean addListener(DBEventListener listener) {
        synchronized (lock) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
                logger.trace("add listener {}", listener.toString());
            }
        }
        return true;
    }

    @Override
    public void listen(EventObject event) {
        for (DBEventListener lin : listeners) {
            if (lin.accept(event)) {
                lin.listen(event);
            }
        }
    }

}