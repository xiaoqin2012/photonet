package net.piaw.photonet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Date;

/**
 * Created by xiaoqin on 11/12/2015.
 */
public class ImageInfo {
    Date date;
    double lat;
    double lon;
    String addrStr;
    String fileName;
    BitmapDescriptor bmd;

    public ImageInfo(Date date_val, double latitude, double longitude, String s1, String file_name) {
        date = date_val;
        lat = latitude;
        lon = longitude;
        addrStr = s1;
        fileName = file_name;
    }

    public void generatedBitmap() {
        Bitmap bm = BitmapFactory.decodeFile(fileName);
        bmd = BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(addWhiteBorder(bm, 200), 80, 80, false));
    }

    public Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }
}
