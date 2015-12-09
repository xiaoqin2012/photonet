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
    int numMarkers = 10 ;
    MediaReadTest mediaReadTest;

    QueryFilter filter = new QueryFilter();
    DatePickerFragment startDateF;
    DatePickerFragment endDateF;

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
        cluster.setText("unCluster");
        createClusterMarkers();
        mIsClustered = true;
        redrawMap();
        startDateF = new DatePickerFragment();
        endDateF = new DatePickerFragment();

        // set listeners
        cluster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(v.toString(), "onClick");
                if (mIsClustered) {
                    cluster.setText("Cluster");
                    mIsClustered = false;
                    filter.clear();
                    redrawMap();
                } else {
                    cluster.setText("Uncluster");
                    createClusterMarkers();
                    mIsClustered = true;
                    filter.clear();
                    redrawMap();
                }
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
        getGoogleMap().clear();
        // clear marker lists
        mMarkers.clear();
        mClusterMarkers.clear();

        // get projection area
        Projection projection = getGoogleMap().getProjection();

        // create random markers
        int i=0;
        for (ImageInfo info : mediaReadTest.imageInfoList.infoList) {
            if (info.marker == null) {
                LatLng markerPos = new LatLng(info.lat, info.lon);
                MarkerOptions markerOptions = new MarkerOptions().position(markerPos).visible(true);
                markerOptions.icon(info.bmd);
                markerOptions.title(i + " " + info.date.toString());
                markerOptions.snippet(info.addrStr);
                Marker marker = getGoogleMap().addMarker(markerOptions);
                info.marker = marker;
                // add to list
                mMarkers.add(marker);
            }
            i++;
        }
        ImageInfo info = mediaReadTest.imageInfoList.infoList.get(0);
        getGoogleMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(info.lat, info.lon), 12));
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
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(cluster.center).visible(false);
                    markerOptions.title(cluster.markers.get(0).getTitle());
                    markerOptions.snippet(cluster.markers.get(0).getSnippet());
                    String[] tokens = cluster.markers.get(0).getTitle().split(" ");
                    ImageInfo info = mediaReadTest.imageInfoList.infoList.get(Integer.parseInt(tokens[0]));
                    markerOptions.icon(info.bmd);

                    Marker clusterMarker = getGoogleMap().addMarker(
                            markerOptions);
                    // add to list
                    mClusterMarkers.add(clusterMarker);
                }
            }
        }
    }

    private void recreateClusterMarkers() {
        // remove cluster markers from map
        for (Marker marker : mClusterMarkers) {
            marker.remove();
        }
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
            for (ImageInfo info: mediaReadTest.imageInfoList.infoList) {
                if(filter.queryMatch(info.date, info.addrStr))
                    info.marker.setVisible(true);
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