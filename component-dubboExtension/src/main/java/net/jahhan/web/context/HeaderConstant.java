package net.jahhan.web.context;

public interface HeaderConstant {
    /**
     * 客户端操作系统，1:Android，2:IOS，3:WP,4:WEB
     */
    String clientos = "clientos";
    /**
     * 客户端操作系统版本号，比如 4.0
     */
    String osversion = "osversion";
    /**
     * 客户端类型，比如iphone 4s
     */
    String clientphone = "clientphone";
    /**
     * 客户端app版本号
     */
    String clientversion = "clientversion";
    /**
     * 客户端唯一编码，有可能是EMMI，也有可能是网卡地址
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
    /**
     * 城市id
     */
    String cityId = "cityid";
    /**
     * 省份id
     */
    String provinceId = "provinceid";
    /**
     * 区域id
     */
    String regionId = "regionid";
    /**
     * 接口版本
     */
    String actVersion = "actversion";
    /**
     * 令牌
     */
    String token = "token";
}
