package net.jahhan.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * CollectionUtil
 *
 * @author longlin(longlin@cyou-inc.com)
 * @date 2013-10-15
 * @since V1.0
 */
public class CollectionUtil {
    public static final String DEFAULT_DELIMITER = ",";

    /**
     * 列表是否为空
     *
     * @param list
     * @return
     */
    public static boolean isEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 列表是否不为空
     *
     * @param list
     * @return
     */
    public static boolean isNotEmpty(Collection<?> list) {
        return !isEmpty(list);
    }

    /**
     * 过滤空
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> List<T> filterEmpty(Collection<T> list) {
        if (list == null) {
            return null;
        }

        List<T> newList = new ArrayList<T>();
        for (T t : list) {
            if (t != null && t.toString().length() > 0) {
                newList.add(t);
            }
        }
        return newList;
    }

    /**
     * 字符串分割
     *
     * @param s
     * @param delimiter
     * @return
     */
    public static List<String> split(String s, String delimiter) {
        List<String> list = new ArrayList<String>();

        if (s != null && s.length() != 0) {
            String[] arr = s.split(delimiter);
            for (int i = 0; i < arr.length; i++) {
                if (StringUtils.isNotEmpty(arr[i])) {
                    list.add(arr[i].trim());
                }
            }
        }

        return list;
    }

    /**
     * 字符串分割
     *
     * @param s
     * @return
     */
    public static List<String> split(String s) {
        return split(s, DEFAULT_DELIMITER);
    }

    /**
     * 将list对象用separator连接起来
     *
     * @param list
     * @param separator
     * @param <T>
     * @return
     */
    public static <T> String join(Collection<T> list, String separator) {
        StringBuilder sb = new StringBuilder();
        if (list != null) {
            int i = 0;
            List<T> newList = filterEmpty(list);
            for (T t : newList) {
                sb.append(t.toString());
                if (i < list.size() - 1) {
                    sb.append(separator);
                }
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * list对象转为Array对象
     *
     * @param list
     * @return
     */
    public static String[] toArray(Collection<String> list) {
        if (list == null) {
            return null;
        }

        String[] ts = new String[list.size()];
        list.toArray(ts);
        return ts;
    }

    /**
     * 数组转List对象
     *
     * @param array
     * @param <T>
     * @return
     */
    public static <T> List<T> asList(T... array) {
        if (array == null) {
            return null;
        }

        List<T> list = new ArrayList<T>();
        for (T t : array) {
            if (t != null) {
                list.add(t);
            }
        }
        return list;
    }

    public static <T extends Object> List<String> trim(List<T> list) {
        if (list == null) {
            return null;
        }
        List<String> newList = new ArrayList<String>();
        for (Object o : list) {
            if (o != null) {
                newList.add(StringUtils.trim(String.valueOf(o)));
            }
        }
        return newList;
    }
}
