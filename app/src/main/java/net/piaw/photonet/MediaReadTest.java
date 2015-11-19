package net.piaw.photonet;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

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
    public static final String CAMERA_IMAGE_BUCKET_NAME =
            Environment.getExternalStorageDirectory().toString()
                    + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID =
            getBucketId(CAMERA_IMAGE_BUCKET_NAME);

    public static ArrayList<String> result = null;

    public final String infoFileName = Environment.getDataDirectory().getAbsolutePath() +
            "image_info_list.txt";
    public File infoFile = new File(infoFileName);

    public static Context context = null;

    public static ImageInfoList imageInfoList = new ImageInfoList();
    public static int numImage = 0;
    /**
     * Matches code in MediaProvider.computeBucketValues. Should be a common
     * function.
     */
    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }

    public static List<String> getCameraImages() {
        final String[] projection = { MediaStore.Images.Media.DATA };
        String[] columns = new String[] {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.SIZE };
        final String selection = MediaStore.Images.Media.MIME_TYPE;
        final String[] selectionArgs = { "image/*" };
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                null,
                null,
                null);
        result = new ArrayList<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                result.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public static Date date = new Date(0);

    public static void scanImageInfoList() throws IOException, InterruptedException {
        for (String file_name : result) {
            File file = new File(file_name);
            ImageProcess imageP = null;
            Date date_t = new Date(file.lastModified());
            if (date_t.compareTo(date) > 0) {
                imageP = new ImageProcess(file.getAbsolutePath(), context);
                if (imageP.isLegal()) {
                    ImageInfo imageInfo = imageP.getImageInfo();
                    imageInfoList.add_one_image_info(imageInfo);
                    if (numImage != 0 && imageInfoList.infoList.size() >= numImage) {
                        break;
                    }
                }
            }
        }


    }

    public static ArrayList<ImageInfo>  getCameraImageMetadata(Context context_t, int num) {
        numImage = num;
        context = context_t;
        String filename = "image_info_list.txt";

        File infoFile = new File(context.getFilesDir()+filename);
        ArrayList<Thread> threads = imageInfoList.start_processing();
        if (infoFile.exists()) {
            ImageDirProcess.readImageInfoList(infoFile, imageInfoList);
        } else {
            try {
                infoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            getCameraImages();
            scanImageInfoList();
        } catch (IOException e) {
                e.printStackTrace();
        } catch (InterruptedException e) {
                e.printStackTrace();
        }

        imageInfoList.set_image_info_end_marker();

        try {
            for (Thread t : threads) {
                t.join();
            }
            ImageDirProcess.writeImageInfoList(imageInfoList.infoList, infoFile);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageInfoList.infoList;
    }
}
