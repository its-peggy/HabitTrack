package com.example.habittrack.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.habittrack.HabitWrapper;
import com.example.habittrack.IconsAdapter;
import com.example.habittrack.ProgressWrapper;
import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import com.example.habittrack.R;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class CreateFragment extends Fragment {

    public static final String TAG = "CreateFragment";

    private List<Habit> habits;
    private List<Progress> progresses;

    private EditText etCreateHabitName;
    private EditText etCreateHabitGoalQty;
    private EditText etCreateHabitUnits;
    private Spinner spCreateTimeOfDay;
    private Spinner spCreateTag;
    private TimePicker tpCreateReminderTime;
    private Button btnCreateHabit;
    private ImageButton ibIconButton;

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

        Bundle bundle = getArguments();
        HabitWrapper hw =(HabitWrapper) bundle.getSerializable("Habit");
        ProgressWrapper pw = (ProgressWrapper) bundle.getSerializable("Progress");
        habits = hw.getHabits();
        progresses = pw.getProgress();

        etCreateHabitName = view.findViewById(R.id.etCreateHabitName);
        etCreateHabitGoalQty = view.findViewById(R.id.etCreateHabitGoalQty);
        etCreateHabitUnits = view.findViewById(R.id.etCreateHabitUnits);
        spCreateTimeOfDay = view.findViewById(R.id.spCreateTimeOfDay);
        spCreateTag = view.findViewById(R.id.spCreateTag);
        tpCreateReminderTime = view.findViewById(R.id.tpCreateReminderTime);
        btnCreateHabit = view.findViewById(R.id.btnCreateHabit);
        ibIconButton = view.findViewById(R.id.ibIconButton);

        ArrayAdapter<CharSequence> adapterTimeOfDay = ArrayAdapter.createFromResource(getActivity(),
                R.array.create_time_of_day_array, android.R.layout.simple_spinner_item);
        adapterTimeOfDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCreateTimeOfDay.setAdapter(adapterTimeOfDay);

        ArrayAdapter<CharSequence> adapterTag = ArrayAdapter.createFromResource(getActivity(),
                R.array.create_tag_array, android.R.layout.simple_spinner_item);
        adapterTag.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCreateTag.setAdapter(adapterTag);

        ibIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup_icon_selection_window, null);

                GridView gridView = (GridView) container.findViewById(R.id.gridView);
                Button btnSaveIcon = (Button) container.findViewById(R.id.btnSaveIcon);

                gridView.setAdapter(new IconsAdapter(getContext()));
                final int[] lastSelected = {0};

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Toast.makeText(getContext(), "icon at position " + position + " clicked", Toast.LENGTH_SHORT).show();
                        lastSelected[0] = position;
                    }
                });

                PopupWindow popupWindow = new PopupWindow(container, 800, 800, true);
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                btnSaveIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Toast.makeText(getContext(), "save button", Toast.LENGTH_SHORT).show();
                        int pos = lastSelected[0]; // TODO: better way to get currently selected position?
                        ibIconButton.setImageResource((Integer) gridView.getItemAtPosition(pos));
                        popupWindow.dismiss();
                    }
                });
            }
        });

        btnCreateHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawableIcon = ibIconButton.getDrawable();
                Bitmap bitmap = ((BitmapDrawable) drawableIcon).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] iconByteArray = stream.toByteArray();
                ParseFile icon = new ParseFile("icon.png", iconByteArray);

                String habitName = etCreateHabitName.getText().toString();
                String habitGoalQtyString = etCreateHabitGoalQty.getText().toString();
                int habitGoalQty = 0;
                if (!habitGoalQtyString.isEmpty()) {
                    habitGoalQty = Integer.parseInt(etCreateHabitGoalQty.getText().toString());
                }
                else {
                    Toast.makeText(getContext(), "Please enter a value for all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
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

                if (habitName.isEmpty() || habitUnits.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a value for all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                Habit habit = new Habit();
                Progress progress = new Progress();
                ParseUser currentUser = ParseUser.getCurrentUser();

                habit.setUser(currentUser);
                habit.setName(habitName);
                habit.setIcon(icon);
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
                        habits.add(habit);
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
                                progresses.add(progress);
                                Log.i(TAG, "Updated progress object with habit ID");

                                HomeFragment homeFragment = new HomeFragment();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("Habit", new HabitWrapper(habits));
                                bundle.putSerializable("Progress", new ProgressWrapper(progresses));
                                homeFragment.setArguments(bundle);
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