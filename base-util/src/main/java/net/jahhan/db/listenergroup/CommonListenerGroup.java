package net.jahhan.db.listenergroup;

import net.jahhan.demand.DBEventListener;

/**
 * 用于监听所有事件
 */
public class CommonListenerGroup extends GenericListenerGroup {

    @Override
    public boolean addListener(DBEventListener listener) {
        if (listener.getTags() != null) {
            return false;
        }
        return super.addListener(listener);
    }

}