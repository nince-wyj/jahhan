package net.jahhan.jdbc.utils;

public class TagUtil {

    public static String[] getTags(Class<?> clz) {
        return new String[] { getType(clz) };
    }

    public static String getType(Class<?> clz) {
        return clz.getSimpleName();
    }

}
