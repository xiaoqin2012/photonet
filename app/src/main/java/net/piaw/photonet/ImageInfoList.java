package net.piaw.photonet;

import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

/**
 * Created by xiaoqin on 11/17/2015.
 */
public class ImageInfoList {
    ArrayList<ImageInfo> infoList;
    Hashtable<String, ImageInfo> hashInfoList;
    boolean imageDirProcessDone = false;
    Semaphore available = new Semaphore(0, true);
    Date recentTime;
    Semaphore countBitmap = new Semaphore(0, true);
    int numAddedTo = 0;
    int index = 0;

    public ImageInfoList(){
        infoList = new ArrayList<ImageInfo>();
        hashInfoList = new Hashtable<String, ImageInfo>();

        imageDirProcessDone = false;
        available = new Semaphore(0, true);
        countBitmap = new Semaphore(0, true);
        numAddedTo = 0;
        index = 0;
        recentTime = new Date(0);
    }

    public boolean process_one_image_info(BitmapFactory.Options opt) throws InterruptedException {
        ImageInfo info = get_one_image_info();
        if (info == null)
            return false;
        info.generatedBitmap(opt);
        countBitmap.release();
        Log.d("ImageInfoList", "countBitmap = " +countBitmap);
        return true;
    }

    public synchronized ImageInfo get_one_image_info() throws InterruptedException {
        if (imageDirProcessDone ) {
            Log.d("imageInfo:", "see end marker index:" + index + " available:" + available);
            return null;
        }
        String s1 = "infoList size: " + infoList.size();
        available.acquire();
        if ( infoList.size() == index) {
            imageDirProcessDone = true;
            Log.d("imageInfo", "put end marker: " + index);
            return null;
        }
        ImageInfo info = infoList.get(index);
        index++;
        Log.d("imageInfo:", "available:" + available + " infoList.size:" + infoList.size() +
                " index: " + index);
        return info;
    }

    public void add_one_image_info(ImageInfo info) {
        ImageInfo old_info = hashInfoList.remove(info.fileName);
        if (old_info != null) {
            old_info.marker.setTitle("remove");
            hashInfoList.put(old_info.fileName + "remove", old_info);
            Log.d("add_one_image_info", "mark remove:" + old_info.fileName +
                    old_info.date + old_info.addrStr);
        }

        File file = new File(info.fileName);
        if (!file.exists()) {
            return;
        }

        hashInfoList.put(info.fileName, info);
        infoList.add(info);
        available.release();
        numAddedTo ++;
        Log.d("add_one_image_info", "infoList:size" + infoList.size() + " " +
                info.fileName + info.date + info.addrStr);
    }

    public void set_image_info_end_marker() {
        available.release();
    }

    public ArrayList<Thread> start_processing(){
        ArrayList<Thread> threads = new ArrayList<Thread>();
        int numThreads = 4;
        final ArrayList<BitmapFactory.Options> options = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = 8;
            option.inTempStorage =new byte[65536];  // allocate 16KB for processingt
            options.add(option);
        }

        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(new TestRunnable(options.get(i), this));
            threads.add(t);
            t.start();
        }
        return threads;
    }
}