package net.jahhan.common.extension.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nince
 */
public class ListUtils {
    private ListUtils() {
    }

    public static String list2Str(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (String str : list) {
            result.append(str).append(",");

        }
        if (result.length() > 1) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }

    public static String integerList2Str(List<Long> list) {
        StringBuilder result = new StringBuilder();
        for (Long str : list) {
            result.append(str).append(",");

        }
        if (result.length() > 1) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }

    /**
     * 将任意类型的list转化为string类型的列表，保持列表的顺序不变
     * 
     * @param list
     * @return
     * @author nince
     */
    public static List<String> list2StrList(List<?> list) {
        List<String> retList = new ArrayList<String>(list.size());
        for (int i = 0; i < list.size(); i++) {
            retList.add(String.valueOf(list.get(i)));
        }
        return retList;
    }
    
    public static List<byte[]> list2ByteList(List<?> list) {
        List<byte[]> retList = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            retList.add(String.valueOf(list.get(i)).getBytes());
        }
        return retList;
    }
}
