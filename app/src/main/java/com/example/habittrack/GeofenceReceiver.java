package com.example.habittrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceReceiver extends BroadcastReceiver {

    public static final String TAG = "GeofenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "received geofence broadcast");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }
        int transitionType = geofencingEvent.getGeofenceTransition();
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, "entered geofence");
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for (Geofence geofence : triggeringGeofences) {
                Log.d(TAG, geofence.toString());
            }
            // Send notification and log the transition details.

        }
    }
}
