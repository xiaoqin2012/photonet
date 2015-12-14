package net.piaw.photonet;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    String dcim = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).getAbsolutePath();

    float mCurrentZoom;
    ArrayList<Marker> mMarkers;
    ArrayList<Marker> mClusterMarkers;
    boolean mIsClustered = false;
    boolean mMapNeedsSetup = true;
    int numMarkers = 0 ;
    MediaReadTest mediaReadTest;

    QueryFilter filter = new QueryFilter();
    DatePickerFragment startDateF;
    DatePickerFragment endDateF;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // initialize marker list
        mMarkers = new ArrayList<Marker>();
        mClusterMarkers = new ArrayList<Marker>();
        mediaReadTest = new MediaReadTest(getApplication().getApplicationContext());

        // initialize ui
        initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check google play services
        int isAvailable = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (isAvailable != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(isAvailable, this, 1).show();
        } else {
            setupMap();
        }
    }

    private void initializeUI() {
        // get ui items
        final Button cluster = (Button) findViewById(R.id.cluster);

        initCrawl(numMarkers);
        initMarkers();
        cluster.setText("refresh");
        createClusterMarkers();
        mIsClustered = false;
        redrawMap();
        startDateF = new DatePickerFragment();
        endDateF = new DatePickerFragment();

        // set listeners
        cluster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter.clear();
                mIsClustered = true;
                initCrawl(numMarkers);
                cleanMarkers();
                initMarkers();
                recreateClusterMarkers();
                redrawMap();
            }
        });

        final Button query = (Button) findViewById(R.id.query);
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter.clear();
                filter.setDate(startDateF.date, endDateF.date);
                mIsClustered = false;
                redrawMap();
            }
        });


        final Button startDate = (Button) findViewById(R.id.startDate);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDateF.show(getFragmentManager(), "datePicker");
            }
        });

        final Button endDate = (Button) findViewById(R.id.endDate);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDateF.show(getFragmentManager(), "datePicker");
            }
        });
    }

    private void setupMap() {
        if (mMapNeedsSetup) {
            if (getGoogleMap() != null) {
                mCurrentZoom = getGoogleMap().getCameraPosition().zoom;
                getGoogleMap().setOnCameraChangeListener(
                        new GoogleMap.OnCameraChangeListener() {
                            @Override
                            public void onCameraChange(
                                    CameraPosition newPosition) {
                                // is clustered?
                                if (mIsClustered
                                        && mCurrentZoom != newPosition.zoom) {
                                    // create cluster markers for new position
                                    recreateClusterMarkers();
                                    // redraw map
                                    redrawMap();
                                }
                                mCurrentZoom = newPosition.zoom;
                            }
                        });
            }
            mMapNeedsSetup = false;
        }
    }

    public void initCrawl(final int numMarkers) {
        ImageDirProcess dirProcess = null;
        Thread t1 = new Thread(new Runnable() {
            public void run() {
                mediaReadTest.getCameraImageMetadata(getApplicationContext(), numMarkers);
            }
        });
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initMarkers() {
        // clear map
        //getGoogleMap().clear();
        // clear marker lists
        mClusterMarkers.clear();

        // get projection area
        Projection projection = getGoogleMap().getProjection();

        // create random markers
        int i=0;
        Enumeration<ImageInfo> e = mediaReadTest.imageInfoList.hashInfoList.elements();
        while (e.hasMoreElements()) {
            ImageInfo info = e.nextElement();
            if (info.marker == null) {
                LatLng markerPos = new LatLng(info.lat, info.lon);
                MarkerOptions markerOptions = new MarkerOptions().position(markerPos).visible(true);
                markerOptions.icon(info.bmd);
                markerOptions.title(info.date.toString());
                markerOptions.snippet(info.addrStr);
                Marker marker = getGoogleMap().addMarker(markerOptions);
                info.marker = marker;
                // add to list
                mMarkers.add(marker);
            }
            i++;
        }
        e = mediaReadTest.imageInfoList.hashInfoList.elements();
        if(e.hasMoreElements()) {
            ImageInfo info = e.nextElement();
            getGoogleMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(info.lat, info.lon), 12));
        } else {
            Log.d("initMarkers:", "hashInfoList.size: " + mediaReadTest.imageInfoList.hashInfoList.size());
            getGoogleMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(375, 375), 12));
        }
    }

    private void createClusterMarkers() {

        if (mClusterMarkers.size() == 0) {
            // set cluster parameters
            int gridSize = 100;
            boolean averageCenter = true;
            // create clusters
            Marker[] markers = mMarkers.toArray(new Marker[mMarkers.size()]);
            ArrayList<MarkerCluster> markerClusters = new MarkerClusterer(
                    getGoogleMap(), markers, gridSize, averageCenter)
                    .createMarkerClusters();
            // create cluster markers
            for (MarkerCluster cluster : markerClusters) {
                int markerCount = cluster.markers.size();
                if (markerCount == 1) {
                    mClusterMarkers.add(cluster.markers.get(0));
                } else {
                    // get marker view and set text
                    View markerView = getLayoutInflater().inflate(
                            R.layout.cluster_marker_view, null);
                    ((TextView) markerView.findViewById(R.id.marker_count))
                            .setText(String.valueOf(markerCount));

                    // create cluster marker
                    //MarkerOptions markerOptions = new MarkerOptions();
                    //markerOptions.position(cluster.center).visible(false);
                    //markerOptions.title(cluster.markers.get(0).getTitle());
                    //markerOptions.snippet(cluster.markers.get(0).getSnippet());
                    //String[] tokens = cluster.markers.get(0).getTitle().split(" ");

                    mClusterMarkers.add(cluster.markers.get(0));
                }
            }
        }
    }

    private void recreateClusterMarkers() {
        // clear cluster markers list
        mClusterMarkers.clear();
        // create mew cluster markers
        createClusterMarkers();
    }

    private void redrawMap() {

        // hide all markers
        for (Marker marker : mMarkers) {
            marker.setVisible(false);
        }
        for (Marker marker : mClusterMarkers) {
            marker.setVisible(false);
        }
        // show markers
        if (mIsClustered) {
            for (Marker marker : mClusterMarkers) {
                marker.setVisible(true);
            }
        } else {
            Enumeration<ImageInfo> e = mediaReadTest.imageInfoList.hashInfoList.elements();
            while (e.hasMoreElements()) {
                ImageInfo info = e.nextElement();
                if (info.date == null)
                    continue;
                if(filter.queryMatch(info.date, info.addrStr)) {
                    info.marker.setVisible(true);
                }
            }
        }

        Log.d("redrawMap", " addedInfo:" + mediaReadTest.imageInfoList.numAddedTo +
                " countBitMap:" + mediaReadTest.imageInfoList.countBitmap);
    }

    private void cleanMarkers() {
        Enumeration<ImageInfo> e = mediaReadTest.imageInfoList.hashInfoList.elements();
        while (e.hasMoreElements()){
            ImageInfo info = e.nextElement();
            File file = new File(info.fileName);
            if (!file.exists()){
                info.marker.remove();
                mMarkers.remove(info.marker);
                Log.d("clearMarkers: file not exist: ", "remove a marker");
                mediaReadTest.imageInfoList.hashInfoList.remove(info);
            }
        }

        for(Marker marker:mMarkers) {
            if(marker.getTitle() == "remove") {
                marker.remove();
                mMarkers.remove(marker);
                Log.d("clearMarkers: modified file set remove ", "remove a marker");
            }
        }
    }

    private GoogleMap getGoogleMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map));
            mMap = mapFragment.getMap();
        }
        return mMap;
    }
}