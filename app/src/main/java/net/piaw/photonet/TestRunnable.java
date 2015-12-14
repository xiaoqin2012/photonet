package net.piaw.photonet;

import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created by xiaoqin on 12/13/2015.
 */
public class TestRunnable implements Runnable {
    BitmapFactory.Options option;
    ImageInfoList infoList;

    public TestRunnable(BitmapFactory.Options opt, ImageInfoList infoList_val) {
        option = opt;
        infoList = infoList_val;
    }

    public void run() {
        while (true) {
            try {
                boolean result = infoList.process_one_image_info(option);
                if (!result)
                    break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            Log.d(this.toString(), " exit");
    }
}
