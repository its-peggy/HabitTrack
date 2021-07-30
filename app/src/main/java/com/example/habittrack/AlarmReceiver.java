package com.example.habittrack;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.habittrack.models.Habit;

import org.apache.commons.lang3.SerializationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "AlarmReceiver";
    public static final String MIDNIGHT_TAG = "MidnightUpdate";
    public static final String TIME_NOTIFY_TAG = "TimeNotification";

    public static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == MIDNIGHT_TAG) {
            setNextMidnightAlarm(context);
            Intent midnightService = new Intent(context, MidnightService.class);
            context.startService(midnightService);
        }
        else if (intent.getAction() == TIME_NOTIFY_TAG) {
            int requestCode = intent.getIntExtra(Habit.KEY_REQUEST_CODE, -1);
            long reminderTimeMillis = intent.getLongExtra(Habit.KEY_REMIND_AT_TIME, -1);
            String habitName = intent.getStringExtra(Habit.KEY_NAME);
            showNotification(context, habitName);
            setNextHabitReminder(context, requestCode, reminderTimeMillis);
        }
    }

    private void showNotification(Context context, String habitName) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, -2, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "asdf")
                .setSmallIcon(R.drawable.ic_fi_rr_bell_ring)
                .setContentTitle(habitName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }

    public void setNextHabitReminder(Context context, int requestCode, long reminderTimeMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(TIME_NOTIFY_TAG);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeMillis + MILLIS_IN_DAY, pendingIntent);
    }

    public void setNextMidnightAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(MIDNIGHT_TAG);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, getMidnightTomorrowInMillis(), pendingIntent);
    }

    private long getMidnightTomorrowInMillis() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime midnightTomorrow = tomorrow.atStartOfDay();
        return midnightTomorrow.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
