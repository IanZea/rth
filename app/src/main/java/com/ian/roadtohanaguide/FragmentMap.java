package com.ian.roadtohanaguide;

import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlGeometry;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IanZ on 12/23/2015.
 */
public class FragmentMap extends android.app.Fragment implements OnMapReadyCallback{
    private static GoogleMap mMap;
    private MapView mMapView;
    private LatLng hana = new LatLng(20.7341357, -156.1639934);
    private SlidingUpPanelLayout slidingUpPanelLayout;

    public static RelativeLayout announceGeofenceLayout;
    public static ImageView announcePlayButton;
    public static TextView announceText;
    public static ListView announceListView;
    public View rootView;

    //<editor-fold desc="Commented out for later">
   /* private Location mCurrentLocation;
    private LatLng lastClickLocation;
    private LatLngBounds MAUI = new LatLngBounds(new LatLng(20.544, -156.867), new LatLng(21.275, -155.64));
    private String mLastUpdateTime;
    private TextView locationTextAddMarker;
    private Button createMarkerButton;
    private EditText nameEditText;
    private EditText detailsEditText;
    private RelativeLayout addMarkerLayout;
    private ImageView exitAddMarkerButton;*/
    //</editor-fold>

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.manualTourMap);
        mMapView.getMapAsync(this);
        mMapView.onCreate(savedInstanceState);


        announceGeofenceLayout = (RelativeLayout) rootView.findViewById(R.id.transitionAnnounce);
        announceText = (TextView) rootView.findViewById(R.id.transitionAnnounceText);
        announceListView = (ListView) rootView.findViewById(R.id.announceLayoutListView);
        slidingUpPanelLayout = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelCollapsed(View panel) {

            }

            @Override
            public void onPanelExpanded(View panel) {

            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });

        return rootView;

        //<editor-fold desc="Old code for adding your own markers in app">
        /** nameEditText = (EditText) rootView.findViewById(R.id.editTextName);
         exitAddMarkerButton = (ImageView) rootView.findViewById(R.id.exitAddMarkerLayoutButton);
         detailsEditText = (EditText) rootView.findViewById(R.id.editTextDetails);
         locationTextAddMarker = (TextView) rootView.findViewById(R.id.clickLocationTextAddMarker);
         addMarkerLayout = (RelativeLayout) rootView.findViewById(R.id.addNewMarkerLayout);
         createMarkerButton = (Button) rootView.findViewById(R.id.addMarkerButton);

         exitAddMarkerButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
        addMarkerLayout.setVisibility(View.INVISIBLE);

        if (rootView != null) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
        }
        }
        });

         /*announceExitButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
        ActivityMain.tts.stop();
        ActivityMain.isPlaying=false;
        FragmentMap.announcePlayButton.setImageResource(R.mipmap.play_white);
        announceGeofenceLayout.setVisibility(View.INVISIBLE);
        }
        });*/
        //</editor-fold>
    }


    //Method needed for viewPagers custom adapter
    public static FragmentMap newInstance() {

        Bundle args = new Bundle();

        FragmentMap fragment = new FragmentMap();
        fragment.setArguments(args);
        return fragment;
    }

    //<editor-fold desc="onMapReady">
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * This method will only be triggered if the user has installed Google Play services.
     */
    //</editor-fold>
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        KmlLayer layer = null;
        //addMarkers(mMap, ActivityMain.mGeofenceInfos);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(hana));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        //If you navigate outside mauis coords the camera will snap back to Maui
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                {
                    int maxZoomOut = 7;
                    if (cameraPosition.zoom < maxZoomOut) {
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                    }
                    if (cameraPosition.target.longitude > -155.73) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(hana));
                    }
                    if (cameraPosition.target.longitude < -156.786) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(hana));
                    }
                    if (cameraPosition.target.latitude > 21.2) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(hana));
                    }
                    if (cameraPosition.target.latitude < 20.37) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(hana));
                    }
                }
            }
        });


        //When you click a marker it will give the announce window its info
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                {
                    final GeofenceInfo info = getMarkerInfo(marker);
                    announceText.setText(info.getRequestId());
                    announceListView.setAdapter(new ActivityMain.AnnounceListAdapter(getResources(), getActivity(), info));
                    announceGeofenceLayout.setVisibility(View.VISIBLE);
                    return false;
                }
            }
        });

        //on a drag(or long click) delete user-created marker
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                removeGeoInfo(marker);
            }
        });

       //These calls make the map useable IN a fragment in a viewpager.  Otherwise it wont load
        mMapView.onResume();
        MapsInitializer.initialize(getActivity());

        try {
            layer = new KmlLayer(mMap, R.raw.rth_polygons, getActivity());
            layer.addLayerToMap();
            //layer.get
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        final KmlLayer finalLayer = layer;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                ArrayList<String> strings = new ArrayList<String>();
/*
                Toast.makeText(getActivity(), latLng.latitude+""+latLng.longitude+"", Toast.LENGTH_LONG).show();
*/
                for(KmlContainer container : finalLayer.getContainers()){
                    for(KmlContainer container1: container.getContainers()){
                        for(KmlPlacemark placemark: container1.getPlacemarks()){
                            KmlGeometry mGeometry = placemark.getGeometry();
                            ArrayList<Object> object = (ArrayList<Object>) mGeometry.getGeometryObject();
                            ArrayList<String> latLngListInString = new ArrayList<String>((Collection<? extends String>) object.get(0));
                            ArrayList<LatLng> latLngList = new ArrayList<LatLng>((Collection<? extends LatLng>) object.get(0));
                            boolean containsPoint = PolyUtil.containsLocation(latLng, latLngList, true);
                            if(containsPoint){
                                Toast.makeText(getActivity(), placemark.getProperty("name"), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
        });



        //The following commented code let the user click to add their own custom markers. It then
        //writes the array to a text document on the devices SD card for later(a simple database)
        //not using it atm
        {
       /* mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                lastClickLocation = latLng;
                addMarkerLayout.setVisibility(View.VISIBLE);
                locationTextAddMarker.setText(String.format("%.2f", lastClickLocation.latitude) + ", " + String.format("%.2f", lastClickLocation.longitude));


                createMarkerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String name = nameEditText.getText().toString();
                        final String details = detailsEditText.getText().toString();
                        if(name.equals("")||name.contains("#")||details.equals("")||details.contains("#")) {
                            Toast.makeText(getActivity(), "Invalid entry", Toast.LENGTH_LONG).show();
                        }
                        else {
                            mMap.addMarker(new MarkerOptions()
                                    .position(lastClickLocation)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            GeofenceInfo info = new GeofenceInfo(name, details, "-1", lastClickLocation);
                            info.setUserCreated(true);
                            ActivityMain.mGeofenceInfos.add(info);
                            writeToFile(ActivityMain.mGeofenceInfos);
                            detailsEditText.setText("");
                            nameEditText.setText("");
                            addMarkerLayout.setVisibility(View.INVISIBLE);

                            if (rootView != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                            }
                        }

                    }
                });
            }
        });*/
        }
    }


    //<editor-fold desc="removeGeoInfo">
    //method to remove a user created marker and its info from the list
    //</editor-fold>
    private void removeGeoInfo(Marker marker){
        ArrayList<GeofenceInfo> infosCopy = new ArrayList<>(ActivityMain.mGeofenceInfos);
        String markerTitle = marker.getTitle();
        for(int i=0; i<infosCopy.size();i++){
            if(infosCopy.get(i).getRequestId().equals(markerTitle)){
                marker.remove();
                ActivityMain.mGeofenceInfos.remove(i);
                writeToFile(ActivityMain.mGeofenceInfos);
                return;
            }
        }

    }


    //<editor-fold desc="getMarkerInfo">
    /**Ive made it so every markers title is unique and equal to the requestID, i can than iterate through list GeofenceInfos
    to find its GeofenceInfo object and get the rest of the details.*/
    //</editor-fold>
    private GeofenceInfo getMarkerInfo(Marker marker){
        GeofenceInfo markerInfo = new GeofenceInfo("null", "null", "-1");
        String title = marker.getTitle();
        for(GeofenceInfo info: ActivityMain.mGeofenceInfos) {
            if (info.getRequestId().equals(title)) {
                return info;
            }
        }
        return markerInfo;
    }


    //<editor-fold desc="writeToFile">
    //write an arraylist to a textfile to be stored for later
    //</editor-fold>
    public void writeToFile(ArrayList<GeofenceInfo> list){
        //This will get the SD Card directory and create a folder named MyFiles in it.
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File (sdCard.getAbsolutePath() + "/RoadToHanaFiles");
        directory.mkdirs();

        //Now create the file in the above directory and write the contents into it
        File file = new File(directory, "OurLocations.txt");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter writer = new PrintWriter(fw);


        for(GeofenceInfo info: list){
            String string = "";
            if(info.isUserCreated()) {
                string += info.getRequestId() + "#";
                string += info.getLocation().latitude + "#";
                string += info.getLocation().longitude + "#";
                string += "-1#";
                string += info.getDescripition()+"";
                writer.print(string);
                writer.println();
            }
        }

        writer.flush();
        writer.close();
    }


    //<editor-fold desc="addMarkers">
    //iterate through the geofenceInfo list and create all the markers
    //</editor-fold>
    private void addMarkers(GoogleMap map, ArrayList<GeofenceInfo> list) {
        for (GeofenceInfo geoInfo : list) {
            if(!geoInfo.isUserCreated()&&!geoInfo.isHeader()) {
                map.addMarker(new MarkerOptions().position(geoInfo.getLocation()).title(geoInfo.getRequestId()));
            }
            else if(!geoInfo.isHeader()){
                map.addMarker(new MarkerOptions().position(geoInfo.getLocation()).title(geoInfo.getRequestId()).draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        }
    }

}
