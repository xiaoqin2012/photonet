package net.piaw.photonet;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String dcim = Environment.getExternalStoragePublicDirectory(
    Environment.DIRECTORY_DCIM).getAbsolutePath();
    ArrayList<ImageInfo> infoList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        ImageDirProcess dirProcess = null;
        mMap = googleMap;
        Thread t1 = new Thread(new Runnable(){
            public void run() {
                //scanDir();
                try {
                    infoList = MediaReadTest.getCameraImageMetadata(getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        drawMap();
    }

    public void drawPoint(ImageInfo info) {// Add a marker in Sydney and move the camera
        if (info == null) {
            Log.e("drawPoint:", "info is null!");
            return;
        }
        LatLng sydney = new LatLng(info.lat, info.lon);
        mMap.addMarker(new MarkerOptions().position(sydney).title(info.dateStr)
                .snippet(info.addrStr));
     }

    public void drawMap() {
        for (ImageInfo info : infoList) {
            Log.v("drawmap", "Drawing one point");
            drawPoint(info);
        }

        int index = infoList.size() / 2;
        if (infoList.get(index) == null) {
            Log.v("drawmap", "midpoint is empty!");
            return;
        }
        LatLng center = new LatLng(infoList.get(index).lat, infoList.get(index).lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 13));
    }
}
