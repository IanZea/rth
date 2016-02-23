package com.ian.roadtohanaguide;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;

/**
 IntentService is a base class for Services that handle asynchronous requests (expressed as Intents) on demand.
 Clients send requests through startService(Intent) calls; the service is started as needed,
 handles each Intent in turn using a worker thread, and stops itself when it runs out of work.
 */


public class GeofenceTransitionsIntentService extends IntentService {
    public static final String TRANSITION_TYPE = "TRANSITION_TYPE";
    public static final String NUMBER_OF_TRIGGERS = "NUMBER_OF_TRIGGERS";
    public static final String GEOFENCE_ID = "GEOFENCE_ID";
    private final String TAG = "Geofence_transitions";


    public GeofenceTransitionsIntentService() {
        super("geo-service");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = geofencingEvent.getErrorCode()+"";
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            ArrayList<Geofence> triggeringGeofences = (ArrayList<Geofence>) geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceString = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );
            //make a push notification
            buildnotification(geofenceString);
            System.out.println("intent Received");


            //create an intent with all this information and broadcast it for ActivityMain's receiver to pick up
            Intent fenceInfo = new Intent(ActivityMain.RECEIVE_JSON);
            fenceInfo.putExtra(NUMBER_OF_TRIGGERS, triggeringGeofences.size());
            fenceInfo.putExtra(GEOFENCE_ID, geofenceString);
            if (geofenceTransition==Geofence.GEOFENCE_TRANSITION_ENTER){
                fenceInfo.putExtra(TRANSITION_TYPE, "Enter");
            }else if (geofenceTransition==Geofence.GEOFENCE_TRANSITION_EXIT){
                fenceInfo.putExtra(TRANSITION_TYPE, "Exit");
            }else if(geofenceTransition==Geofence.GEOFENCE_TRANSITION_DWELL){
                fenceInfo.putExtra(TRANSITION_TYPE, "Dwell");
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(fenceInfo);

        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));

        }



    }

    private void buildnotification(String geofenceTransitionDetails) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_media_play)
                        .setContentTitle("Geofence!")
                        .setContentText(geofenceTransitionDetails);

        mBuilder.setStyle(new NotificationCompat.BigTextStyle(mBuilder).bigText(geofenceTransitionDetails).setBigContentTitle("Big title")
                .setSummaryText(geofenceTransitionDetails));


        // Send notification and log the transition details.
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private String getGeofenceTransitionDetails(GeofenceTransitionsIntentService geofenceTransitionsIntentService, int geofenceTransition, ArrayList<Geofence> triggeringGeofences) {
        String string = "";
        for(Geofence geo: triggeringGeofences){
            string += geo.getRequestId()+"#";
        }
        return string;
    }
}
