/**
 * Created by xiaoqin on 11/12/2015.
 */
package net.piaw.photonet;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import android.os.Environment;

public class ImageDirProcess {
    public static String INIT_TIMESTAMP = "19600101 000000";
    public final String infoFileName = Environment.getDataDirectory().getAbsolutePath() +
            "image_info_list.txt";
    public File infoFile = new File(infoFileName);

    File dirFile;
    Context context;

    List<ImageInfo> imageInfoList;
    String latestTimestamp;

    //read from a file first
    //the file is a fixed name in the system.
    //the constructor will read the existing file
    public ImageDirProcess(String dir_name, Context cont) throws IOException {
        //get the latest timestamp
        context = cont;
        latestTimestamp = INIT_TIMESTAMP;

        if (infoFile.exists()) {
            readImageInfoList();
        }

        scanImageInfoList(dir_name);
    }

    public void readImageInfoList(){
    }

    public void writeImageInfoList() {
            //sort imageinfor list
            //get the latest timestamp
            //start to read
    }

    public void scanImageInfoList(String path) throws IOException {
        File dir = new File(path);
        File[] flist = dir.listFiles();

        for (File file : flist) {
            ImageProcess imageP = null;
            Date date = new Date(file.lastModified());
            if (date.toString().compareTo(latestTimestamp) > 0) {
                if (file.isDirectory())
                    scanImageInfoList(file.getAbsolutePath());
                else if (file.isFile()) {
                    imageP = new ImageProcess(file.getAbsolutePath(), context);
                    if (imageP.isLegal() && imageP.getDateStr().compareTo(latestTimestamp) > 0) {
                        ImageInfo imageInfo = imageP.getImageInfo();
                        imageInfoList.add(imageInfo);
                    }
                }
            }
        }
    }
    public List<ImageInfo> getImageInfoList() {
        return imageInfoList;
    }

}
