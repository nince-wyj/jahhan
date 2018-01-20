package net.jahhan.spi;

import java.util.EventObject;

public interface DBEventListener {
	int getExistSecond();
    /**
     * 是否接受当前的事件
     * 
     * @param event
     * @return
     */
    boolean accept(EventObject event);

    void listen(EventObject event);

    /**
     * 所属group的标签,null表示监听全部，空数组表示全部不监听
     * 
     * @return
     */
    String[] getTags();
}
