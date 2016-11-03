package net.jahhan.cache.util;

import java.util.Date;

public class PkUtils {
    /*
     * 多个主键拼接
     */
    public static String PKtoString(Object... objs) {
        String pkstring = "";
        String pk = "";
        for (Object obj : objs) {
            if (Date.class.isAssignableFrom(obj.getClass())) {
                Date d = (Date) obj;
                pk = String.valueOf(d.getTime());
            } else {
                pk = String.valueOf(obj);
            }
            pkstring += pkstring.equals("") ? pk : "," + pk;
        }
        return pkstring;
    }

    /**
     * 用于从pojo获取redis主键的时候使用
     * 
     * @param obj
     * @return
     */
    public static String valueOf(Object obj) {
        return obj == null ? null : obj.toString();
    }
}
