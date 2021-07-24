package com.example.habittrack;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class MidnightService extends Service {

    public static final String TAG = "MidnightService";
    Context context;

    public MidnightService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        queryDatabase();
    }

    private void queryDatabase() {
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
                for (Habit habit : queriedHabits) {
                    Progress oldProgress = habit.getTodayProgress();
                    Progress newProgress = new Progress();

                    newProgress.setUser(habit.getUser());
                    newProgress.setHabit(habit);
                    newProgress.setDate(tomorrowDate());
                    newProgress.setQtyCompleted(0);
                    newProgress.setQtyGoal(habit.getQtyGoal());
                    newProgress.setPctCompleted(0);
                    newProgress.setCompleted(false);

                    habit.setTodayProgress(newProgress);
                    if (oldProgress.getCompleted()) {
                        habit.setStreak(habit.getStreak()+1);
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
                        Intent intent = new Intent("queried-database");
                        HabitWrapper hw = new HabitWrapper(queriedHabits);
                        intent.putExtra("queriedHabits", hw);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                });
            }
        });
    }

    private String tomorrowDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String tomorrowFormatted = formatter.format(tomorrow);
        return tomorrowFormatted;
    }

    public LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: what goes here?
        throw new UnsupportedOperationException("Not yet implemented");
    }
}