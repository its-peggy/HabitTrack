package com.example.habittrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.habittrack.fragments.CreateFragment;
import com.example.habittrack.fragments.HomeFragment;
import com.example.habittrack.fragments.ProfileFragment;
import com.example.habittrack.fragments.ProgressFragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Location;
import com.example.habittrack.models.OverallProgress;
import com.example.habittrack.models.Progress;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;
    private GeofencingClient geofencingClient;

    Activity activity;

    List<Geofence> geofenceList = new ArrayList<>();
    List<Habit> habitList = new ArrayList<>();
    List<OverallProgress> overallProgressList = new ArrayList<>();

    private Map<String, Double> dateToTotalProgressPct = new TreeMap<>();
    private Map<String, Integer> dateToProgressCount = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        geofencingClient = LocationServices.getGeofencingClient(this);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_progress:
                        fragment = new ProgressFragment();
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        break;
                    default:
                        fragment = new HomeFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);

        queryAvailableLocations();

        createOverallProgresses();

        createNotificationChannel();

        setMidnightAlarm();

    }

    private void createOverallProgresses() {
        if (overallProgressList.isEmpty()) {
            ParseQuery<Progress> queryProgressEntries = ParseQuery.getQuery(Progress.class);
            queryProgressEntries.whereEqualTo(Progress.KEY_USER, ParseUser.getCurrentUser());
            queryProgressEntries.setLimit(1000);
            queryProgressEntries.addAscendingOrder(Progress.KEY_DATE);
            queryProgressEntries.include(Progress.KEY_USER);
            queryProgressEntries.include(Progress.KEY_HABIT);
            queryProgressEntries.findInBackground(new FindCallback<Progress>() {
                @Override
                public void done(List<Progress> progressList, ParseException e) {
                    for (Progress progress : progressList) {
                        String date = progress.getDate();
                        if (!dateToTotalProgressPct.containsKey(date)) {
                            dateToTotalProgressPct.put(date, progress.getPctCompleted());
                        } else {
                            double currentTotalPct = dateToTotalProgressPct.get(date);
                            dateToTotalProgressPct.put(date, currentTotalPct + progress.getPctCompleted());
                        }
                        if (!dateToProgressCount.containsKey(date)) {
                            dateToProgressCount.put(date, 1);
                        } else {
                            int currentTotalCount = dateToProgressCount.get(date);
                            dateToProgressCount.put(date, currentTotalCount + 1);
                        }
                    }
                    for (Map.Entry<String, Double> entry : dateToTotalProgressPct.entrySet()) {
                        String date = entry.getKey();
                        Double totalPct = entry.getValue();
                        int totalCount = dateToProgressCount.get(date);
                        OverallProgress overallProgress = new OverallProgress();
                        overallProgress.setUser(ParseUser.getCurrentUser());
                        overallProgress.setDate(date);
                        overallProgress.setNumHabits(totalCount);
                        overallProgress.setOverallPct(totalPct / totalCount);
                        overallProgressList.add(overallProgress);
                    }
                    ParseObject.saveAllInBackground(overallProgressList, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error saving OverallProgress entries");
                            }
                            Log.d(TAG, "Successfully saved OverallProgress entries");
                        }
                    });
                }
            });
        }
    }

    private Boolean permissionsGranted() {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permissions granted");
                addGeofenceForLocation(Location.getLocationObjectByName("Home"));
                addGeofences();
            } else {
                Log.e(TAG, "User did not grant location permissions");
                Toast.makeText(this, "User did not grant location permissions", Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private void queryAvailableLocations() {
        ParseQuery<Location> queryLocations = ParseQuery.getQuery(Location.class);
        queryLocations.whereEqualTo(Location.KEY_USER, ParseUser.getCurrentUser());
        queryLocations.include(Location.KEY_USER);
        queryLocations.findInBackground(new FindCallback<Location>() {
            @Override
            public void done(List<Location> locationList, ParseException e) {
                for (Location location : locationList) {
                    Location.allLocationNames.add(location.getName());
                    Location.nameToLocationObject.put(location.getName(), location);
                }
                if (!Location.nameToLocationObject.isEmpty()) {
                    if (!permissionsGranted()) {
                        Log.d(TAG, "here");
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                123);
                    } else {
                        for (Location location : locationList) {
                            addGeofenceForLocation(location);
                        }
                        addGeofences();
                    }
                }
            }
        });
    }

    private void createNotificationChannel() {
        String name = "channel_0";
        String description = "this is channel 0";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("asdf", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("type").equals("updated-progress-entries")) {
                HabitWrapper hw = (HabitWrapper) intent.getSerializableExtra("queriedHabits");
                List<Habit> queriedHabits = hw.getHabits();
                setHabitList(queriedHabits);
            } else if (intent.getStringExtra("type").equals("new-overall-progress")) {
                OverallProgressWrapper wrapper = (OverallProgressWrapper) intent.getSerializableExtra("newOverallProgress");
                List<OverallProgress> overallProgressList = wrapper.getOverallProgressList();
                setOverallProgressList(overallProgressList);
            }
            else {
                Log.d(TAG, "LocalBroadcast type not recognized");
            }
            queryAvailableLocations(); // re-set geofences
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("midnight-service"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void setMidnightAlarm() {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.MIDNIGHT_TAG);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, getMidnightTomorrowInMillis(), pendingIntent);
    }

    private long getMidnightTomorrowInMillis() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime midnightTomorrow = tomorrow.atStartOfDay();
        return midnightTomorrow.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void addGeofenceForLocation(Location location) {
        geofenceList.add(new Geofence.Builder()
                .setRequestId(location.getName())
                .setCircularRegion(
                        location.getLocation().getLatitude(),
                        location.getLocation().getLongitude(),
                        150)
                .setExpirationDuration(1000 * 60 * 60 * 24)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private void addGeofences() {
        GeofencingRequest geofencingRequest = getGeofencingRequest();
        Intent intent = new Intent("com.example.GEOFENCE_BROADCAST").setPackage(getPackageName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, -10, intent, 0);

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Log.e(TAG, "Location permissions not granted");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    123);
        }

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Geofences added successfully");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failure to add geofences", e);
                    }
                });
    }

    public List<Habit> getHabitList() {
        return habitList;
    }

    public void setHabitList(List<Habit> habits) {
        habitList = habits;
    }

    public List<OverallProgress> getOverallProgressList() {
        return overallProgressList;
    }

    public void setOverallProgressList(List<OverallProgress> overallProgresses) {
        overallProgressList = overallProgresses;
    }

}