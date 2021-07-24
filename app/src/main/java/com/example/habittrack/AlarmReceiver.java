package com.example.habittrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        setNextAlarm(context);
        Intent midnightService = new Intent(context, MidnightService.class);
        context.startService(midnightService);
    }

    public void setNextAlarm(Context context) {
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

}
