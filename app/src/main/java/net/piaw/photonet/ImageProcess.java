package net.piaw.photonet;    /**
     * Created by xiaoqin on 11/12/2015.
     */

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.drew.imaging.ImageMetadataReader.readMetadata;

public class ImageProcess {
    File file;
    Metadata metadata;
    String fileName;
    //String dateStr;
    Date date;
    GeoLocation gps;
    //file.absolutePath()
    Address addr;
    boolean legal;
    String addrStr;
    Context context;

    /**
     * Constructor which executes multiple sample usages, each of which return the same output.  This class showcases
     * multiple usages of this metadata class library.
     *
     * @param file_name path to a file upon which to operate
     */
    public ImageProcess(String file_name, Context cont) throws IOException {
        fileName = file_name;
        file = new File(file_name);
        context = cont;

        legal = true;
        try {
            metadata = readMetadata(file);
        } catch (ImageProcessingException e) {
            legal = false;
            System.err.println("error 1a: " + e);
        } catch (IOException e) {
            legal = false;
            System.err.println("error 1b: " + e);
        }

        if (!legal)
            return;
        try {
            setTimeOriginal();
            setGPSInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTimeOriginal() {
        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        //List[Directory] test = metadata.getDirectories();
        if (directory == null) {
            legal = false;
            return;
        }

        date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        Log.d("date: ", " " + date);
        if (date == null) {
            date = new Date();
        }
    }

    public void setGPSInfo() throws IOException {
        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDirectory == null) {
            legal = false;
            return;
        }
        gps = gpsDirectory.getGeoLocation();
        if (gps == null) {
            Log.e("setGPSInfo", "GPS is null!");
            legal = false;
            return;
        }

        setAddr();
    }

    public void setAddr() throws IOException {
        Geocoder geocoder = new Geocoder(context);
        List<Address> test = geocoder.getFromLocation(gps.getLatitude(), gps.getLongitude(), 1);
        Log.d("setAddr:" + gps.getLatitude(), test.toString() );
        List<Address> addresses =  geocoder.getFromLocation(gps.getLatitude(),gps.getLongitude(), 1);
        if (addresses == null || addresses.size() == 0) {
            addrStr = "null";
            return;
        }

        addr = addresses.get(0);
        if (addr == null) {
            addrStr = "null";
            return;
        }
        addrStr = addr.getAddressLine(0);
        if (addr.getAddressLine(1)!= null)
            addrStr = addrStr + " " + addr.getAddressLine(1);
        if (addr.getAddressLine(2)!= null)
            addrStr = addrStr + " " + addr.getAddressLine(2);
    }

    private void printImageTags()
    {
        System.out.println();
        // iterate over the exif data and print to System.out
        for (Directory directory : metadata.getDirectories()) {
            for (com.drew.metadata.Tag tag : directory.getTags())
                Log.v("printImageTags", tag.toString());
            for (String error : directory.getErrors())
                Log.e("printImageTags()", "ERROR: " + error);
        }
    }

    public ImageInfo getImageInfo()
    {
        if (gps == null) {
            Log.e("getImageInfo", "GPS is null!");
            return null;
        }

        return new ImageInfo(date, gps.getLatitude(),gps.getLongitude(),
                addrStr, file.getAbsolutePath());
     }

    public String getDateStr() {
        return date.toString();
    }

    public boolean isLegal() { return legal; }
}

