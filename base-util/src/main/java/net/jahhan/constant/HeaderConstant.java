package net.jahhan.constant;

public interface HeaderConstant {

    /**
     * 操作系统，101：Android，102：IOS，103：WP,104:WEB,105:PC
     */
    String clientos = "clientos";

    /**
     * 手机操作系统版本号，比如 4.0
     */
    String osversion = "osversion";

    /**
     * 手机类型，比如iphone 4s
     */
    String clientphone = "clientphone";

    /**
     * 手机app版本号
     */
    String version = "version";

    /**
     * 手机唯一编码，有可能是EMMI，也有可能是网卡地址
     */
    String phoneuuid = "phoneuuid";

    /**
     * 维度
     */
    String lat = "lat";

    /**
     * 经度
     */
    String lon = "lon";

    String cityId = "city_id";
    
    String cityName = "city_name";

    String regionId = "regionId";

}
