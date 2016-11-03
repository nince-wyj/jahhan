package net.jahhan.web.action;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import net.jahhan.constant.enumeration.FilterTypeEnum;
import net.jahhan.web.action.filterhandler.JsonFilterHandler;

/**
 * @author nince
 */
@Singleton
public class FilterHandlerFactory {

    private static final Map<FilterTypeEnum, FilterHandler> filterMap = new HashMap<FilterTypeEnum, FilterHandler>(4, 1);

    static {
        filterMap.put(FilterTypeEnum.ESCAPE, new JsonFilterHandler());
    }

    public FilterHandler getFieldTypeHandler(FilterTypeEnum filterTypeEnum) {
        return filterMap.get(filterTypeEnum);
    }
}
