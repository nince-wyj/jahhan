package net.jahhan.db.event;

import java.util.Collection;

/**
 * 用于根据某种id查询的类型.比如productImage，他是根据productID查询的
 * 
 * @author nince
 */
public class ListEvent extends DBEvent {

    /**
	 * 
	 */
    private static final long serialVersionUID = -3786807208667396149L;

    /**
     * 外键id，非空.多个外键就用PkUtils组合起来使用
     */
    private String foreignId;

    public String getForeignId() {
        return foreignId;
    }

    public ListEvent(Collection<?> list, String type, String foreignId) {
        super(list, type, EventOperate.LISTByParent, null);
        this.foreignId = foreignId;
    }

    @Override
    public String toString() {
        return "ListEvent [foreignId=" + foreignId + ", getType()=" + getType() + "]";
    }

}
