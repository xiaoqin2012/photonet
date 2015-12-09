package net.piaw.photonet;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by xiaoqin on 11/14/2015.
 */
public class MediaReadTest {
    public final static String CAMERA_IMAGE_BUCKET_NAME =
            Environment.getExternalStorageDirectory().toString()
                    + "/DCIM/Camera";
    public final static String CAMERA_IMAGE_BUCKET_ID =
            getBucketId(CAMERA_IMAGE_BUCKET_NAME);

    public ArrayList<String> cameraImagList;
    public String infoFileName ;
    public File infoFile;
    public Context context;
    public ImageInfoList imageInfoList;
    public int numImage;
    public boolean refresh;

    MediaReadTest(Context applicationContext){
        cameraImagList = new ArrayList<>();
        context = applicationContext;

        infoFileName = "image_info_list.txt";
        infoFile = new File(context.getFilesDir()+infoFileName);
        imageInfoList = new ImageInfoList();
        numImage = 0;
        refresh = false;
    }

    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    public void getCameraImages() {
        String[] columns = new String[] {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.SIZE };
        final String selection = MediaStore.Images.Media.MIME_TYPE;
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                null,
                null,
                null);
        if (cursor == null || cursor.getCount() == 0)
            return;
        cameraImagList = new ArrayList<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                cameraImagList.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void scanImageInfoList() throws IOException, InterruptedException {
        for (String file_name : cameraImagList) {
            File file = new File(file_name);
            ImageProcess imageP;
            Date date_t = new Date(file.lastModified());
            if (date_t.compareTo(imageInfoList.recentTime) > 0) {
                imageP = new ImageProcess(file.getAbsolutePath(), context);
                if (imageP.isLegal()) {
                    ImageInfo imageInfo = imageP.getImageInfo();
                    Log.d("imageInfo: " + imageP.toString(), " " + imageInfo);
                    imageInfoList.add_one_image_info(imageInfo);
                    if (numImage != 0 && imageInfoList.infoList.size() >= numImage) {
                        break;
                    }
                }
            }
        }
    }

    public void getCameraImageMetadata(Context context_t, int num) {
        numImage = num;
        context = context_t;
        String filename = "image_info_list.txt";
        Date date;

        ArrayList<Thread> threads = imageInfoList.start_processing();
        if (!refresh) {
            readInfoFile();
            refresh = true;
        }

        date = new Date();
        if (numImage == 0 || imageInfoList.infoList.size() < numImage) {
            try {
                getCameraImages();
                scanImageInfoList();
              } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        imageInfoList.set_image_info_end_marker();
        imageInfoList.recentTime = date;

        try {
            for (Thread t : threads) {
                t.join();
            }
            ImageDirProcess.writeImageInfoList(imageInfoList, infoFile);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readInfoFile() {
        if (infoFile.exists()) {
            ImageDirProcess.readImageInfoList(infoFile, imageInfoList, numImage);
        } else {
            try {
                infoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
