package com.example.habittrack.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import com.example.habittrack.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class CreateFragment extends Fragment {

    public static final String TAG = "CreateFragment";

    private EditText etCreateHabitName;
    private EditText etCreateHabitGoalQty;
    private EditText etCreateHabitUnits;
    private Spinner spCreateTimeOfDay;
    private Spinner spCreateTag;
    private TimePicker tpCreateReminderTime;
    private Button btnCreateHabit;

    public CreateFragment() {};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCreateHabitName = view.findViewById(R.id.etCreateHabitName);
        etCreateHabitGoalQty = view.findViewById(R.id.etCreateHabitGoalQty);
        etCreateHabitUnits = view.findViewById(R.id.etCreateHabitUnits);
        spCreateTimeOfDay = view.findViewById(R.id.spCreateTimeOfDay);
        spCreateTag = view.findViewById(R.id.spCreateTag);
        tpCreateReminderTime = view.findViewById(R.id.tpCreateReminderTime);
        btnCreateHabit = view.findViewById(R.id.btnCreateHabit);

        ArrayAdapter<CharSequence> adapterTimeOfDay = ArrayAdapter.createFromResource(getActivity(),
                R.array.create_time_of_day_array, android.R.layout.simple_spinner_item);
        adapterTimeOfDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCreateTimeOfDay.setAdapter(adapterTimeOfDay);

        ArrayAdapter<CharSequence> adapterTag = ArrayAdapter.createFromResource(getActivity(),
                R.array.create_tag_array, android.R.layout.simple_spinner_item);
        adapterTag.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCreateTag.setAdapter(adapterTag);

        btnCreateHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String habitName = etCreateHabitName.getText().toString();
                int habitGoalQty = Integer.parseInt(etCreateHabitGoalQty.getText().toString());
                String habitUnits = etCreateHabitUnits.getText().toString();
                String timeOfDay = spCreateTimeOfDay.getSelectedItem().toString();
                String tag = spCreateTag.getSelectedItem().toString();
                int reminderHour = tpCreateReminderTime.getHour();
                int reminderMinute = tpCreateReminderTime.getMinute();
                LocalTime nextReminderTime = LocalTime.of(reminderHour, reminderMinute);
                LocalDate nextReminderDate;
                if (nextReminderTime.isBefore(LocalTime.now())) {
                    nextReminderDate = LocalDate.now().plusDays(1); // tomorrow
                } else {
                    nextReminderDate = LocalDate.now(); // today
                }
                LocalDateTime nextReminderDateTime = LocalDateTime.of(nextReminderDate, nextReminderTime);
                Date reminderDateObject = Date.from(nextReminderDateTime.atZone(ZoneId.systemDefault()).toInstant());

                Habit habit = new Habit();
                Progress progress = new Progress();
                ParseUser currentUser = ParseUser.getCurrentUser();

                habit.setUser(currentUser);
                habit.setName(habitName);
                // habit.setIcon(); // TODO: let user select icon
                habit.setTodayProgress(progress);
                habit.setTag(tag);
                habit.setQtyGoal(habitGoalQty);
                habit.setUnit(habitUnits);
                habit.setTimeOfDay(timeOfDay);
                habit.setStreak(0);
                habit.setRemindAtTime(reminderDateObject);

                progress.setUser(currentUser);
                String todayDate = Progress.getTodayDateString();
                progress.setDate(todayDate);
                progress.setQtyCompleted(0);
                progress.setQtyGoal(habitGoalQty);
                progress.setPctCompleted(0);
                progress.setCompleted(false);

                habit.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error while saving habit", e);
                            return;
                        }
                        Log.i(TAG, "Habit save was successful!");
                        Progress progress = habit.getTodayProgress();
                        progress.setHabit(habit);
                        progress.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Error associating habit ID to progress object");
                                    return;
                                }
                                Log.i(TAG, "Updated progress object with habit ID");

                                HomeFragment homeFragment = new HomeFragment();
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.flContainer, homeFragment, "findThisFragment")
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    }
                });

            }
        });

    }

}