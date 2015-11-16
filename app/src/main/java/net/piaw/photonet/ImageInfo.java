package net.piaw.photonet;

/**
 * Created by xiaoqin on 11/12/2015.
 */
public class ImageInfo {
    String dateStr;
    double lat;
    double lon;
    String addrStr;
    String fileName;

    public ImageInfo(String s, double latitude, double longitude, String s1, String file_name) {
        dateStr = s;
        lat = latitude;
        lon = longitude;
        addrStr = s1;
        fileName = file_name;
    }
}
