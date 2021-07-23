package com.example.habittrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.habittrack.fragments.CreateFragment;
import com.example.habittrack.fragments.HomeFragment;
import com.example.habittrack.fragments.ProfileFragment;
import com.example.habittrack.fragments.ProgressFragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;

    List<Habit> habitList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        fragment = new HomeFragment();
                        break;
                    case R.id.action_create:
                        fragment = new CreateFragment();
                        break;
                    case R.id.action_progress:
                        fragment = new ProgressFragment();
                        break;
                    case R.id.action_profile:
                    default:
                        fragment = new ProfileFragment();
                        break;
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("Habit", new HabitWrapper(habitList));
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime midnightTomorrow = tomorrow.atStartOfDay();
        long millisMidnightTomorrow = midnightTomorrow.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, millisMidnightTomorrow, pendingIntent);
        Toast.makeText(this, "setting next alarm...", Toast.LENGTH_SHORT).show();
    }

    public List<Habit> getHabitList() {
        return habitList;
    }
    public void setHabitList(List<Habit> habits) {
        habitList = habits;
    }
}