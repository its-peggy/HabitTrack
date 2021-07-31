package com.example.habittrack;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Location;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeofenceReceiver extends BroadcastReceiver {

    public static final String TAG = "GeofenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(context);
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
            List<String> geofenceNames = new ArrayList<>();
            for (Geofence geofence : triggeringGeofences) {
                Log.d(TAG, geofence.toString());
                geofenceNames.add(geofence.getRequestId());
            }
            geofencingClient.removeGeofences(geofenceNames);
            ParseQuery<Habit> queryHabits = ParseQuery.getQuery(Habit.class);
            queryHabits.whereEqualTo(Habit.KEY_USER, ParseUser.getCurrentUser());
            queryHabits.include(Habit.KEY_REMIND_AT_LOCATION);
            queryHabits.findInBackground(new FindCallback<Habit>() {
                @Override
                public void done(List<Habit> habitList, ParseException e) {
                    for (Habit habit : habitList) {
                        Location location = habit.getRemindAtLocation();
                        if (location != null && geofenceNames.contains(location.getName())) {
                            showNotification(context, habit.getName());
                        }
                    }
                }
            });
        }
    }

    private void showNotification(Context context, String habitName) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, -20, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "asdf")
                .setSmallIcon(R.drawable.ic_fi_rr_bell_ring)
                .setContentTitle(habitName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}
