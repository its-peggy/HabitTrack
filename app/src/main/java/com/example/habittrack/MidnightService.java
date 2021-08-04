package com.example.habittrack;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MidnightService extends Service {

    public static final String TAG = "MidnightService";
    Context context;

    int num_habits = 0;

    public MidnightService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        createNewProgressEntries();
        createNewOverallProgressEntry();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNewProgressEntries() {
        ParseQuery<Habit> queryHabits = ParseQuery.getQuery(Habit.class);
        queryHabits.include(Habit.KEY_USER);
        queryHabits.include(Habit.KEY_TODAY_PROGRESS);
        queryHabits.addAscendingOrder(Habit.KEY_CREATED_AT);
        queryHabits.whereEqualTo(Habit.KEY_USER, ParseUser.getCurrentUser());
        queryHabits.findInBackground(new FindCallback<Habit>() {
            @Override
            public void done(List<Habit> queriedHabits, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with querying habits from Parse", e);
                    return;
                }
                Log.d(TAG, "Successfully queried habits from Parse");
                num_habits = queriedHabits.size();
                for (Habit habit : queriedHabits) {
                    Progress oldProgress = habit.getTodayProgress();
                    Progress newProgress = new Progress();

                    newProgress.setUser(habit.getUser());
                    newProgress.setHabit(habit);
                    newProgress.setDate(todayDate());
                    newProgress.setQtyCompleted(0);
                    newProgress.setQtyGoal(habit.getQtyGoal());
                    newProgress.setPctCompleted(0);
                    newProgress.setCompleted(false);

                    habit.setTodayProgress(newProgress);

                    if (oldProgress.getCompleted()) {
                        habit.setStreak(habit.getStreak()+1);
                    } else {
                        habit.setStreak(0);
                    }

                    if (habit.getStreak() > habit.getLongestStreak()) {
                        habit.setLongestStreak(habit.getStreak());
                    }

                    if (habit.getRemindAtTime() != null) {
                        Date currentReminder = habit.getRemindAtTime();
                        LocalDateTime currentReminderLDT = convertToLocalDateTime(currentReminder);
                        LocalDateTime tomorrowReminderLDT = currentReminderLDT.plusDays(1);
                        Date tomorrowReminder = convertToDate(tomorrowReminderLDT);
                        habit.setRemindAtTime(tomorrowReminder);
                    }

                }
                ParseObject.saveAllInBackground(queriedHabits, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Issue saving habits with updated Progress pointers", e);
                            return;
                        }
                        Log.d(TAG, "Successfully saved habits with updated Progress pointers");
                        Intent intent = new Intent("midnight-service");
                        intent.setAction("updated-progress-entries");
                        HabitWrapper hw = new HabitWrapper(queriedHabits);
                        intent.putExtra("queriedHabits", hw);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                });
            }
        });
    }

    private void createNewOverallProgressEntry() {
        OverallProgress overallProgress = new OverallProgress();
        overallProgress.setUser(ParseUser.getCurrentUser());
        overallProgress.setDate(todayDate());
        overallProgress.setNumHabits(num_habits);
        overallProgress.setOverallPct(0);
        overallProgress.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error saving new OverallProgress entry", e);
                }
                Log.d(TAG, "Successfully saved new OverallProgress entry");
                Intent intent = new Intent("midnight-service");
                intent.setAction("new-overall-progress");
                intent.putExtra("newOverallProgress", (Serializable) overallProgress);
            }
        });
    }

    private String todayDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayFormatted = formatter.format(today);
        return todayFormatted;
    }

    public LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}