package com.example.habittrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.habittrack.fragments.HomeFragment;
import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "AlarmReceiver";

    public final int FREQUENCY = 10000;

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm!!!", Toast.LENGTH_SHORT).show();
        // createNewProgressEntries(context); // TODO: why doesn't this method get called
        setNextAlarm(context);
    }

    public void setNextAlarm(Context context) {
        Toast.makeText(context, "setting next alarm...", Toast.LENGTH_SHORT).show();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, getTomorrowMidnight(), pendingIntent);
    }

    public long getTomorrowMidnight() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime midnightTomorrow = tomorrow.atStartOfDay();
        return midnightTomorrow.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // TODO: move this somewhere else
    public void createNewProgressEntries(Context context) {
        Toast.makeText(context, "making new progress entries", Toast.LENGTH_SHORT).show();
        List<Habit> habits = new ArrayList<>();
        Log.d(TAG, "querying database");
        ParseQuery<Habit> queryHabits = ParseQuery.getQuery(Habit.class);
        queryHabits.include(Habit.KEY_USER);
        queryHabits.include(Habit.KEY_TODAY_PROGRESS);
        queryHabits.whereEqualTo(Habit.KEY_USER, ParseUser.getCurrentUser());
        queryHabits.findInBackground(new FindCallback<Habit>() {
            @Override
            public void done(List<Habit> queriedHabits, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with querying habits from Parse", e);
                    return;
                }
                habits.clear();
                habits.addAll(queriedHabits);
                for (Habit habit : habits) {
                    Progress newProgress = new Progress();

                    LocalDate tomorrow = LocalDate.now().plusDays(1);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-mm-dd");
                    String tomorrowFormatted = formatter.format(tomorrow);

                    newProgress.setUser(habit.getUser());
                    newProgress.setHabit(habit);
                    newProgress.setDate(tomorrowFormatted);
                    newProgress.setQtyCompleted(0);
                    newProgress.setQtyGoal(habit.getQtyGoal());
                    newProgress.setPctCompleted(0);
                    newProgress.setCompleted(false);

                    habit.setTodayProgress(newProgress);

                    habit.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error while saving habit", e);
                                return;
                            }
                            Log.i(TAG, "Habit save was successful!");
                            Toast.makeText(context, "uhhhhh habit saved?", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

}
