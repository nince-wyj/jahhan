package net.jahhan.utils;

public class LatLonUtils {
    private LatLonUtils() {
    }

    private static final double PI = 3.14159265;

    private static final double EARTH_RADIUS = 6378137;

    private static final double RAD = Math.PI / 180.0;

    public static double[] getAround(double lat, double lon, int raidus) {
        double latitude = lat;
        double longitude = lon;
        double degree = (24901 * 1609) / 360.0;
        double raidusMile = raidus;
        double dpmLat = 1 / degree;
        double radiusLat = dpmLat * raidusMile;
        double minLat = latitude - radiusLat;
        double maxLat = latitude + radiusLat;
        double mpdLng = degree * Math.cos(latitude * (PI / 180));
        double dpmLng = 1 / mpdLng;
        double radiusLng = dpmLng * raidusMile;
        double minLng = longitude - radiusLng;
        double maxLng = longitude + radiusLng;
        return new double[] { minLat, minLng, maxLat, maxLng };
    }

    /**
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     */
    public static double getDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = lat1 * RAD;
        double radLat2 = lat2 * RAD;
        double a = radLat1 - radLat2;
        double b = (lng1 - lng2) * RAD;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

}
