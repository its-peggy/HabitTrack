package com.example.habittrack.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.habittrack.AlarmReceiver;
import com.example.habittrack.IconsAdapter;
import com.example.habittrack.MainActivity;
import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Location;
import com.example.habittrack.models.Progress;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
import java.util.Map;

import static android.content.Context.ALARM_SERVICE;

public class CreateFragment extends Fragment {

    public static final String TAG = "CreateFragment";

    private List<Habit> habits;
    private Context context;

    private EditText etCreateHabitName;
    private EditText etCreateHabitGoalQty;
    private EditText etCreateHabitUnits;
    private TimePicker tpCreateReminderTime;
    private Button btnCreateHabit;
    private ImageButton ibIconButton;

    private ChipGroup chipGroupTimeOfDay;
    private ChipGroup chipGroupTag;
    private ChipGroup chipGroupReminderType;
    private ChipGroup chipGroupLocations;

    public CreateFragment() {};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        habits = ((MainActivity)getActivity()).getHabitList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BottomNavigationView bottomNav = ((MainActivity)getActivity()).findViewById(R.id.bottomNavigation);
        bottomNav.setVisibility(View.GONE);
        return inflater.inflate(R.layout.fragment_create, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCreateHabitName = view.findViewById(R.id.etDetailHabitName);
        etCreateHabitGoalQty = view.findViewById(R.id.etDetailHabitGoalQty);
        etCreateHabitUnits = view.findViewById(R.id.etDetailHabitUnits);
        tpCreateReminderTime = view.findViewById(R.id.tpDetailReminderTime);
        btnCreateHabit = view.findViewById(R.id.btnDetailSaveHabit);
        ibIconButton = view.findViewById(R.id.ibDetailIconButton);

        chipGroupTimeOfDay = view.findViewById(R.id.chipGroupTimeOfDay);
        chipGroupTag = view.findViewById(R.id.chipGroupTag);
        chipGroupReminderType = view.findViewById(R.id.chipGroupReminderType);
        chipGroupLocations = view.findViewById(R.id.chipGroupLocations);

        for (int i = 0; i < chipGroupLocations.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLocations.getChildAt(i);
            String locationName = chip.getText().toString();
            if (!Location.allLocationNames.contains(locationName)) {
                chip.setVisibility(View.GONE);
//                chip.setCheckable(false);
//                chip.setChipBackgroundColorResource(R.color.medium_grey);
            }
        }

        chipGroupReminderType.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if (checkedId == R.id.chipTime) {
                    tpCreateReminderTime.setVisibility(View.VISIBLE);
                    chipGroupLocations.setVisibility(View.GONE);
                } else if (checkedId == R.id.chipLocation) {
                    tpCreateReminderTime.setVisibility(View.GONE);
                    chipGroupLocations.setVisibility(View.VISIBLE);
                }
            }
        });

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
                        ibIconButton.setImageBitmap((Bitmap) gridView.getItemAtPosition(pos));
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

                int requestCode = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

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

                int checkedChipTimeOfDayId = chipGroupTimeOfDay.getCheckedChipId();
                Chip checkedChipTimeOfDay = view.findViewById(checkedChipTimeOfDayId);
                String timeOfDay = checkedChipTimeOfDay.getText().toString();

                int checkedChipTagId = chipGroupTag.getCheckedChipId();
                Chip checkedChipTag = view.findViewById(checkedChipTagId);
                String tag = checkedChipTag.getText().toString();

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
                habit.setLongestStreak(0);
                habit.setRequestCode(requestCode);

                if (chipGroupReminderType.getCheckedChipId() == R.id.chipTime) {
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
                    long reminderTimeMillis = nextReminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    habit.setRemindAtTime(reminderDateObject);

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                    Intent intent = new Intent(context, AlarmReceiver.class);
                    intent.setAction(AlarmReceiver.TIME_NOTIFY_TAG);
                    intent.putExtra(Habit.KEY_NAME, habit.getName());
                    intent.putExtra(Habit.KEY_REQUEST_CODE, habit.getRequestCode());
                    intent.putExtra(Habit.KEY_REMIND_AT_TIME, reminderTimeMillis);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, habit.getRequestCode(), intent, 0);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);
                } else if (chipGroupReminderType.getCheckedChipId() == R.id.chipLocation) {
                    int checkedChipLocationId = chipGroupLocations.getCheckedChipId();
                    Chip checkedChipLocation = view.findViewById(checkedChipLocationId);
                    String locationName = checkedChipLocation.getText().toString();
                    Location reminderLocation = Location.getLocationObjectByName(locationName);
                    habit.setRemindAtLocation(reminderLocation);
                }

                progress.setUser(currentUser);
                progress.setDate(Progress.getTodayDateString());
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
                                ((MainActivity)getActivity()).setHabitList(habits);
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