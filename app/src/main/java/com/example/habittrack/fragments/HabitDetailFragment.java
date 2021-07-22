package com.example.habittrack.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.habittrack.R;
import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class HabitDetailFragment extends Fragment {

    public static final String TAG = "HabitDetailFragment";
    protected Habit habit;
    protected Progress progress;

    private EditText etEditHabitName;
    private EditText etEditHabitGoalQty;
    private EditText etEditHabitUnits;
    private Spinner spEditTimeOfDay;
    private Spinner spEditTag;
    private TimePicker tpEditReminderTime;
    private Button btnEditHabit;
    private ImageButton ibDetailIconButton;

    public HabitDetailFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        HabitWrapper hw = (HabitWrapper) bundle.getSerializable("Habit");
        List<Habit> habits = hw.getHabits();
        int position = bundle.getInt("Position");
        habit = habits.get(position);
        progress = habit.getTodayProgress();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habit_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEditHabitName = view.findViewById(R.id.etEditHabitName);
        etEditHabitGoalQty = view.findViewById(R.id.etEditHabitGoalQty);
        etEditHabitUnits = view.findViewById(R.id.etEditHabitUnits);
        spEditTimeOfDay = view.findViewById(R.id.spEditTimeOfDay);
        spEditTag = view.findViewById(R.id.spEditTag);
        tpEditReminderTime = view.findViewById(R.id.tpEditReminderTime);
        btnEditHabit = view.findViewById(R.id.btnEditHabit);
        ibDetailIconButton = view.findViewById(R.id.ibDetailIconButton);

        ArrayAdapter<CharSequence> adapterTimeOfDay = ArrayAdapter.createFromResource(getActivity(),
                R.array.create_time_of_day_array, android.R.layout.simple_spinner_item);
        adapterTimeOfDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEditTimeOfDay.setAdapter(adapterTimeOfDay);

        ArrayAdapter<CharSequence> adapterTag = ArrayAdapter.createFromResource(getActivity(),
                R.array.create_tag_array, android.R.layout.simple_spinner_item);
        adapterTag.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEditTag.setAdapter(adapterTag);

        etEditHabitName.setText(habit.getName());
        etEditHabitGoalQty.setText(Integer.toString(habit.getQtyGoal()));
        etEditHabitUnits.setText(habit.getUnit());
        spEditTimeOfDay.setSelection(adapterTimeOfDay.getPosition(habit.getTimeOfDay()));
        spEditTag.setSelection(adapterTag.getPosition(habit.getTag()));

        LocalDateTime localDateTime = habit.getRemindAtTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        tpEditReminderTime.setHour(localDateTime.getHour());
        tpEditReminderTime.setMinute(localDateTime.getMinute());

        ParseFile icon = habit.getIcon();
        icon.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error retrieving habit icon");
                    return;
                }
                Bitmap bitmapIcon = BitmapFactory.decodeByteArray(data, 0, data.length);
                ibDetailIconButton.setImageBitmap(bitmapIcon);
            }
        });

        ibDetailIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup_icon_selection_window, null);

                GridView gridView = (GridView) container.findViewById(R.id.gridView);
                Button btnSaveIcon = (Button) container.findViewById(R.id.btnSaveIcon);

                gridView.setAdapter(new IconsAdapter(getContext()));
                final int[] lastSelected = {0};

                gridView.setSelection(0); // TODO: how to retrieve position from icon drawable?

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
                        ibDetailIconButton.setImageResource((Integer) gridView.getItemAtPosition(pos));
                        popupWindow.dismiss();
                    }
                });
            }
        });

        btnEditHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String habitName = etEditHabitName.getText().toString();
                int habitGoalQty = Integer.parseInt(etEditHabitGoalQty.getText().toString());
                String habitUnits = etEditHabitUnits.getText().toString();
                String timeOfDay = spEditTimeOfDay.getSelectedItem().toString();
                String tag = spEditTag.getSelectedItem().toString();
                int reminderHour = tpEditReminderTime.getHour();
                int reminderMinute = tpEditReminderTime.getMinute();
                LocalTime nextReminderTime = LocalTime.of(reminderHour, reminderMinute);
                LocalDate nextReminderDate;
                if (nextReminderTime.isBefore(LocalTime.now())) {
                    nextReminderDate = LocalDate.now().plusDays(1); // tomorrow
                } else {
                    nextReminderDate = LocalDate.now(); // today
                }
                LocalDateTime nextReminderDateTime = LocalDateTime.of(nextReminderDate, nextReminderTime);
                Date reminderDateObject = Date.from(nextReminderDateTime.atZone(ZoneId.systemDefault()).toInstant());

                habit.setName(habitName);
                // habit.setIcon(); // TODO: let user select icon
                habit.setTag(tag);
                habit.setQtyGoal(habitGoalQty);
                habit.setUnit(habitUnits);
                habit.setTimeOfDay(timeOfDay);
                habit.setRemindAtTime(reminderDateObject);

                progress.setQtyGoal(habitGoalQty);
                if (progress.getQtyCompleted() >= habitGoalQty) {
                    progress.setQtyCompleted(habitGoalQty);
                    progress.setCompleted(true);
                }

                habit.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error while saving habit", e);
                            return;
                        }
                        Log.i(TAG, "Habit save was successful!");
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
}