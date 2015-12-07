package net.piaw.photonet;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

/**
 * Created by xiaoqin on 11/17/2015.
 */
public class ImageInfoList {
    ArrayList<ImageInfo> infoList;
    HashSet<String> infoHash;
    boolean imageDirProcessDone = false;
    Semaphore available = new Semaphore(0, true);
    int index = 0;
    Date recentTime;

    public ImageInfoList(){
        infoList = new ArrayList<ImageInfo>();
        infoHash = new HashSet<String>();
        imageDirProcessDone = false;
        available = new Semaphore(0, true);
        index = 0;
        recentTime = new Date(0);
    }
    public boolean process_one_image_info() throws InterruptedException {
        ImageInfo info = get_one_image_info();
        if (info == null)
            return false;
        info.generatedBitmap();
        return true;
    }

    public synchronized ImageInfo get_one_image_info() throws InterruptedException {
        if (imageDirProcessDone ) {
            return null;
        }
        String s1 = " index " + index + " size: " + infoList.size();
        available.acquire();
        if (index == infoList.size()) {
            imageDirProcessDone = true;
            return null;
        }
        ImageInfo info = infoList.get(index);
        index++;
        Log.d(this.toString(), "index: " + index);
        return info;
    }

    public void add_one_image_info(ImageInfo info) {
        if (infoHash.contains(info.fileName))
            return;
        infoHash.add(info.fileName);
        infoList.add(info);
        available.release();
    }

    public void set_image_info_end_marker() {
        available.release();
    }

    public ArrayList<Thread> start_processing(){
        ArrayList<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < 1; i++) {
            Thread t = new Thread(new Runnable(){
                public void run() {
                    //scanDir();

                    while (true) {
                        try {
                            boolean result = process_one_image_info();
                            if(!result)
                                break;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(this.toString(), " exit");
                }
            });
            threads.add(t);
            t.start();
        }
        return threads;
    }
}
