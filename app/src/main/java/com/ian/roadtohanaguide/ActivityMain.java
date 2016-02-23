package com.ian.roadtohanaguide;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

/**
 * See FragmentMap class to see how map works
 * This class handles connecting to GoogleApiClient(location services)
 * It creates the lists of geofences from a text document in the assets folder, see OnConnected
 */
public class ActivityMain extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback, TextToSpeech.OnInitListener {
    public static GoogleApiClient gApiClient;
    public static Location mCurrentLocation;
    public static String mLastUpdateTime;
    public static ArrayList<Geofence> mGeofenceList;
    public static ArrayList<GeofenceInfo> mGeofenceInfos;
    public static PendingIntent mGeofencePendingIntent;
    public static TextToSpeech tts;
    public static boolean isPlaying = false;
    public static final String RECEIVE_JSON = "com.ian.roadtohanaguide.RECEIVE_JSON";
    public static Location mLastLocation;

    /*public static ArrayList<GeofenceInfo> userCreatedGeofence;
    * public static ArrayList<GeofenceInfo> mGeofenceInfosSortedByDistance;*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMap);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayoutMap);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPagerMap);

        mGeofenceInfos = new ArrayList<>();
        mGeofenceList = new ArrayList<>();
        tts = new TextToSpeech(this, this);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                FragmentMap.announcePlayButton.setImageResource(R.mipmap.stop);
                isPlaying = true;
            }

            @Override
            public void onDone(String utteranceId) {
                FragmentMap.announcePlayButton.setImageResource(R.mipmap.play_white);
                isPlaying = false;
            }

            @Override
            public void onError(String utteranceId) {

            }
        });

        setSupportActionBar(toolbar);
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        tabLayout.addTab(tabLayout.newTab().setText("Tour By Mile Marker"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.setAdapter(new PagerAdapter(getFragmentManager()));
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIVE_JSON);
        bManager.registerReceiver(bReceiver, intentFilter);

        mGeofenceList = getGeofencesFromTextDoc();

        //set up google location services stuff
        if (gApiClient == null) {
            gApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    //<editor-fold desc="Create Geofences from database">
    /**read text document from assets folder(make shift database), custom parsing,
     * make list of geofence objects and geofencInfo objects(see GeofenceInfo class)*/
    private ArrayList<Geofence> getGeofencesFromTextDoc() {

        AssetManager am = getAssets();
        ArrayList<Geofence> geoList = new ArrayList<>();
        String requestId;
        String tourArea;
        int orderNumber;
        String mileMarker;
        String description;
        double latitude;
        double longitude;

        try {
            InputStream is = am.open("locations.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = reader.readLine()) != null) {
                Scanner scanner = new Scanner(line);
                scanner.useDelimiter("#");

                int isHeader = scanner.nextInt();
                if(isHeader==1){
                    orderNumber=scanner.nextInt();
                    requestId=scanner.next();
                    GeofenceInfo info = new GeofenceInfo(true, requestId);
                    info.setOrderForManualTour(orderNumber);
                    mGeofenceInfos.add(info);
                }
                else {

                    int num = scanner.nextInt();
                    if (num == 0) {
                        tourArea = "Road to Hana";
                    } else {
                        tourArea = "NA";
                    }
                    orderNumber = scanner.nextInt();

                    requestId = scanner.next();
                    latitude = scanner.nextDouble();
                    longitude = scanner.nextDouble();
                    mileMarker = scanner.next();
                    description = scanner.next();


                    geoList.add(createGeofence(requestId, latitude, longitude));
                    GeofenceInfo info = new GeofenceInfo(requestId, description, mileMarker, new LatLng(latitude, longitude));
                    info.setOrderForManualTour(orderNumber);
                    info.setTourSection(tourArea);
                    mGeofenceInfos.add(info);
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard.getAbsolutePath()+"/RoadToHanaFiles", "OurLocations.txt");

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(reader!=null) {
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    Scanner scanner = new Scanner(line);
                    scanner.useDelimiter("#");

                    requestId = scanner.next();
                    latitude = scanner.nextDouble();
                    longitude = scanner.nextDouble();
                    mileMarker = scanner.next();
                    description = scanner.next();


                    geoList.add(createGeofence(requestId, latitude, longitude));
                    GeofenceInfo info = new GeofenceInfo(requestId, description, mileMarker, new LatLng(latitude, longitude));
                    info.setUserCreated(true);
                    mGeofenceInfos.add(info);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return geoList;
    }

    private Geofence createGeofence(String requestId, double latitude, double longitude){
        Geofence fence = new Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(latitude, longitude, 400)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(10000)
                .build();
        return fence;
    }
    //</editor-fold>

    //<editor-fold desc="BroadcastReceiver">
    //Receive and use information processed by GeofenceTransitionsIntentService (see that class for more info)
    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(RECEIVE_JSON)) {
                int numberOfTriggers = intent.getIntExtra(GeofenceTransitionsIntentService.NUMBER_OF_TRIGGERS, -1);
                String triggerIds = intent.getStringExtra(GeofenceTransitionsIntentService.GEOFENCE_ID);
                Scanner scanner = new Scanner(triggerIds);
                scanner.useDelimiter("#");
                String id = scanner.next();
                String description = null;
                GeofenceInfo fenceInfo = null;

                for(GeofenceInfo info: mGeofenceInfos){
                    String infoString = info.getRequestId();
                    if(id.equals(infoString)){
                        description = info.getDescripition();
                        fenceInfo = info;
                    }
                }

                FragmentMap.announceText.setText(id);
                FragmentMap.announceListView.setAdapter(new AnnounceListAdapter(getResources(), getParent(), fenceInfo));
                final String finalDescription = description;
                FragmentMap.announcePlayButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!isPlaying) {
                            if (tts != null) {
                                tts.stop();
                            }
                            speakOut(finalDescription);
                            isPlaying = true;
                            FragmentMap.announcePlayButton.setImageResource(R.mipmap.stop);
                        } else {
                            tts.stop();
                            isPlaying = false;
                            FragmentMap.announcePlayButton.setImageResource(R.mipmap.play_white);
                        }
                    }
                });


            }
        }
    };
    //</editor-fold>

    //<editor-fold desc="OnConnected">
    /**
     * very important Callback for when you connect, this one sets up onLocationChanged to trigger every 5-10 seconds
     * it also sets up all geofences, see methods, let me know if you have questions
     *
     */
    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                gApiClient);

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(gApiClient, mLocationRequest, this);

        LocationServices.GeofencingApi.addGeofences(
                gApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);


        //<editor-fold desc="Code to update distances from current location">
        /*updateThoseDistances(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        sortGeofenceInfosDistance();*/
        //</editor-fold>
    }
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }
    //</editor-fold>

    //<editor-fold desc="OnLocationChanged">
    //do stuff with your location
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        //<editor-fold desc="Hiatus code">
        /**This lets me calculate distances from my location to every geofence location
         * not using it right now
         * updateThoseDistances(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        sortGeofenceInfosDistance();
        updateUI();
        if(FragmentNearestLocations.adapter!=null){
            FragmentNearestLocations.adapter.notifyDataSetChanged();
        }*/
        //</editor-fold>

    }
    private void updateThoseDistances(LatLng location){
        for(GeofenceInfo info: mGeofenceInfos){
            double distance = SphericalUtil.computeDistanceBetween(location, info.getLocation());
            info.setDistanceBetween(distance);
        }
    }
    //</editor-fold>





    //<editor-fold desc="Custom Adapters">
    /**
    Custom Adapter class for ViewPager, lets you have your own custom fragment(and layout) for each
    swipeable page
 */
    private class PagerAdapter extends FragmentPagerAdapter{


        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0){
                return FragmentMap.newInstance();
            }
            else{
                return FragmentManualTour.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    //Custom adapter for the geofence triggered pop up window's listView.  This is for the description text
    //having the description text as a listView saves space and lets people scroll if they want to read more.
    public static class AnnounceListAdapter extends BaseAdapter{
        Activity a;
        Resources res;
        LayoutInflater inflater;
        GeofenceInfo info;

        public AnnounceListAdapter(Resources res, Activity a, GeofenceInfo info){
            this.res = res;
            this.a = a;
            inflater = (LayoutInflater) a.getSystemService(LAYOUT_INFLATER_SERVICE);
            this.info = info;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = inflater.inflate(R.layout.list_item_announce_pop_up_text, parent, false);
            TextView mileMarkerText = (TextView) v.findViewById(R.id.mileMarkerTV);
            TextView paragraphText = (TextView) v.findViewById(R.id.listItemParagraphTV);

            mileMarkerText.setText("Mile Marker 15");
            paragraphText.setText(info.getDescripition());
            return v;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Misc Methods, press ctrl+b to track a method">

    protected void onStart() {
        gApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        gApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onResult(Result result) {


    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    public static void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public static void sortGeofenceInfosOrderNumber() {
        class GeofenceInfoComparator implements Comparator<GeofenceInfo>{

            @Override
            public int compare(GeofenceInfo lhs, GeofenceInfo rhs) {
                if(lhs.getOrderForManualTour()>rhs.getOrderForManualTour()){
                    return 1;
                }
                else if(lhs.getDistanceBetween()<rhs.getDistanceBetween()){
                    return -1;
                }
                else{
                    return 0;
                }
            }
        }
        Collections.sort(mGeofenceInfos, new GeofenceInfoComparator());
    }

    public void sortGeofenceInfosDistance() {
        class GeofenceInfoComparator implements Comparator<GeofenceInfo>{

            @Override
            public int compare(GeofenceInfo lhs, GeofenceInfo rhs) {
                if(lhs.getDistanceBetween()>rhs.getDistanceBetween()){
                    return 1;
                }
                else if(lhs.getDistanceBetween()<rhs.getDistanceBetween()){
                    return -1;
                }
                else{
                    return 0;
                }
            }
        }
        Collections.sort(mGeofenceInfos, new GeofenceInfoComparator());
    }
    //</editor-fold>

    //<editor-fold desc="WIP methods">
    /*public static void hide_keyboard_from(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }*/

    /*public static void speakToFile(String text){
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File (sdCard.getAbsolutePath() + "/RoadToHanaFiles");
        directory.mkdirs();

        File file = new File(directory, "OurLocations.wav");

        File file = new File();
        tts.synthesizeToFile(text);

    }*/
    //</editor-fold>
}
