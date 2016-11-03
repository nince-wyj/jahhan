package net.jahhan.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nelson
 */
public class PageUtils {
    private PageUtils() {
    }

    public static <T> List<T> getPageList(List<T> dataList, int currentIndex, int pageCount) {
        int start = (currentIndex - 1) * pageCount;
        int end = start + pageCount;
        List<T> resultList;
        if (dataList != null && dataList.size() > start && dataList.size() > end) {
            resultList = dataList.subList(start, end);
        } else if (dataList != null && dataList.size() > start && dataList.size() <= end) {
            resultList = dataList.subList(start, dataList.size());
        } else {
            resultList = new ArrayList<T>();
        }
        return resultList;
    }

    public static int getTotalPage(int totalCount, int pageCount) {
        int totalPage = 0;
        if (pageCount > 0) {
            if (totalCount % pageCount == 0) {
                totalPage = totalCount / pageCount;
            } else {
                totalPage = (totalCount / pageCount) + 1;
            }
        }
        return totalPage;
    }
}
