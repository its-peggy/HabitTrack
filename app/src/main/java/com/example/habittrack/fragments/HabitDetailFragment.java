package com.example.habittrack.fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.habittrack.AlarmReceiver;
import com.example.habittrack.HabitWrapper;
import com.example.habittrack.IconsAdapter;
import com.example.habittrack.MainActivity;
import com.example.habittrack.R;
import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Location;
import com.example.habittrack.models.OverallProgress;
import com.example.habittrack.models.Progress;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class HabitDetailFragment extends Fragment {

    public static final String TAG = "HabitDetailFragment";
    protected List<Habit> habits;
    protected Habit habit;
    protected int position;
    protected Progress progress;

    private Context context;

    private TextInputLayout etDetailHabitName;
    private TextInputLayout etDetailHabitGoalQty;
    private TextInputLayout etDetailHabitUnits;
    private TimePicker tpDetailReminderTime;
    private Button btnDetailSaveHabit;
    private Button btnDeleteHabit;
    private ImageButton ibDetailIconButton;

    private ChipGroup chipGroupTimeOfDay;
    private ChipGroup chipGroupTag;
    private ChipGroup chipGroupRepeat;
    private ChipGroup chipGroupReminderType;
    private ChipGroup chipGroupLocations;

    public HabitDetailFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        habits = ((MainActivity)getActivity()).getHabitList();

        BottomNavigationView bottomNavBar = getActivity().findViewById(R.id.bottomNavigation);
        bottomNavBar.setVisibility(View.GONE);

        Bundle bundle = getArguments();
        position = bundle.getInt("Position");
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

        etDetailHabitName = view.findViewById(R.id.etDetailHabitName);
        etDetailHabitGoalQty = view.findViewById(R.id.etDetailHabitGoalQty);
        etDetailHabitUnits = view.findViewById(R.id.etDetailHabitUnits);
        tpDetailReminderTime = view.findViewById(R.id.tpDetailReminderTime);
        btnDetailSaveHabit = view.findViewById(R.id.btnDetailSaveHabit);
        btnDeleteHabit = view.findViewById(R.id.btnDeleteHabit);
        ibDetailIconButton = view.findViewById(R.id.ibDetailIconButton);

        chipGroupTimeOfDay = view.findViewById(R.id.chipGroupTimeOfDay);
        chipGroupTag = view.findViewById(R.id.chipGroupTag);
        chipGroupRepeat = view.findViewById(R.id.chipGroupRepeat);
        chipGroupReminderType = view.findViewById(R.id.chipGroupReminderType);
        chipGroupLocations = view.findViewById(R.id.chipGroupLocations);

        etDetailHabitName.getEditText().setText(habit.getName());
        etDetailHabitGoalQty.getEditText().setText(Integer.toString(habit.getQtyGoal()));
        etDetailHabitUnits.getEditText().setText(habit.getUnit());

        for (int i = 0; i < chipGroupLocations.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLocations.getChildAt(i);
            String locationName = chip.getText().toString();
            if (!Location.allLocationNames.contains(locationName)) {
                chip.setVisibility(View.GONE);
            }
        }

        String habitTimeOfDay = habit.getTimeOfDay();
        for (int i = 0; i < chipGroupTimeOfDay.getChildCount(); i++){
            Chip chip = (Chip) chipGroupTimeOfDay.getChildAt(i);
            String chipText = chip.getText().toString();
            if (chipText.equals(habitTimeOfDay)){
                chipGroupTimeOfDay.check(chip.getId());
            }
        }

        String habitTag = habit.getTag();
        for (int i = 0; i < chipGroupTag.getChildCount(); i++){
            Chip chip = (Chip) chipGroupTag.getChildAt(i);
            String chipText = chip.getText().toString();
            if (chipText.equals(habitTag)){
                chipGroupTag.check(chip.getId());
            }
        }

        List<Integer> remindOnDays = habit.getRepeatOnDays();
        if (remindOnDays == null) {
            remindOnDays = Arrays.asList(7, 1, 2, 3, 4, 5, 6);
        }
        for (int day : remindOnDays) {
            chipGroupRepeat.check(chipGroupRepeat.getChildAt(day % 7).getId());
        }

        boolean timeReminder = (habit.getRemindAtTime() != null);
        if (timeReminder) {
            chipGroupReminderType.check(R.id.chipTime);
            tpDetailReminderTime.setVisibility(View.VISIBLE);
            chipGroupLocations.setVisibility(View.GONE);
            LocalDateTime localDateTime = habit.getRemindAtTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            tpDetailReminderTime.setHour(localDateTime.getHour());
            tpDetailReminderTime.setMinute(localDateTime.getMinute());
        } else {
            chipGroupReminderType.check(R.id.chipLocation);
            tpDetailReminderTime.setVisibility(View.GONE);
            chipGroupLocations.setVisibility(View.VISIBLE);
            String reminderLocationName = habit.getRemindAtLocation().getName();
            for (int i = 0; i < chipGroupLocations.getChildCount(); i++){
                Chip chip = (Chip) chipGroupLocations.getChildAt(i);
                String chipText = chip.getText().toString();
                if (chipText.equals(reminderLocationName)){
                    chipGroupLocations.check(chip.getId());
                }
            }
        }

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
                        lastSelected[0] = position;
                    }
                });

                PopupWindow popupWindow = new PopupWindow(container, 800, 800, true);
                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

                btnSaveIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = lastSelected[0]; // TODO: better way to get currently selected position?
                        ibDetailIconButton.setImageBitmap((Bitmap) gridView.getItemAtPosition(pos));
                        popupWindow.dismiss();
                    }
                });
            }
        });

        btnDetailSaveHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawableIcon = ibDetailIconButton.getDrawable();
                Bitmap bitmap = ((BitmapDrawable) drawableIcon).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] iconByteArray = stream.toByteArray();
                ParseFile icon = new ParseFile("icon.png", iconByteArray);

                String habitName = etDetailHabitName.getEditText().getText().toString();
                int habitGoalQty = Integer.parseInt(etDetailHabitGoalQty.getEditText().getText().toString());
                String habitUnits = etDetailHabitUnits.getEditText().getText().toString();

                int checkedChipTimeOfDayId = chipGroupTimeOfDay.getCheckedChipId();
                Chip checkedChipTimeOfDay = view.findViewById(checkedChipTimeOfDayId);
                String timeOfDay = checkedChipTimeOfDay.getText().toString();

                int checkedChipTagId = chipGroupTag.getCheckedChipId();
                Chip checkedChipTag = view.findViewById(checkedChipTagId);
                String tag = checkedChipTag.getText().toString();

                List<Integer> updatedRepeatOnDays = new ArrayList<>();
                for (int i = 0; i < chipGroupRepeat.getChildCount(); i++){
                    Chip chip = (Chip) chipGroupRepeat.getChildAt(i);
                    if (chip.isChecked()) {
                        if (i == 0) {
                            updatedRepeatOnDays.add(7);
                        } else {
                            updatedRepeatOnDays.add(i);
                        }
                    }
                }

                habit.setName(habitName);
                habit.setIcon(icon);
                habit.setTag(tag);
                habit.setQtyGoal(habitGoalQty);
                habit.setUnit(habitUnits);
                habit.setTimeOfDay(timeOfDay);
                habit.setRepeatOnDays(updatedRepeatOnDays);

                if (chipGroupReminderType.getCheckedChipId() == R.id.chipTime) {
                    LocalDateTime currentReminderDateTime = habit.getRemindAtTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    LocalTime currentReminderTime = currentReminderDateTime.toLocalTime();
                    int reminderHour = tpDetailReminderTime.getHour();
                    int reminderMinute = tpDetailReminderTime.getMinute();
                    LocalTime nextReminderTime = LocalTime.of(reminderHour, reminderMinute);
                    LocalDate nextReminderDate;
                    if (nextReminderTime.isBefore(LocalTime.now())) {
                        nextReminderDate = LocalDate.now().plusDays(1); // tomorrow
                    } else {
                        nextReminderDate = LocalDate.now(); // today
                    }
                    LocalDateTime nextReminderDateTime = LocalDateTime.of(nextReminderDate, nextReminderTime);
                    Date reminderDateObject = Date.from(nextReminderDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    habit.setRemindAtTime(reminderDateObject);

                    if (!nextReminderTime.equals(currentReminderTime)) {
                        long reminderTimeMillis = nextReminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                        Intent intent = new Intent(context, AlarmReceiver.class);
                        intent.setAction(AlarmReceiver.TIME_NOTIFY_TAG);
                        intent.putExtra(Habit.KEY_NAME, habit.getName());
                        intent.putExtra(Habit.KEY_REQUEST_CODE, habit.getRequestCode());
                        intent.putExtra(Habit.KEY_REMIND_AT_TIME, reminderTimeMillis);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, habit.getRequestCode(), intent, 0);
                        alarmManager.cancel(pendingIntent);

                        PendingIntent newPendingIntent = PendingIntent.getBroadcast(context, habit.getRequestCode(), intent, 0);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeMillis, newPendingIntent);
                    }
                } else {
                    int checkedChipLocationId = chipGroupLocations.getCheckedChipId();
                    Chip checkedChipLocation = view.findViewById(checkedChipLocationId);
                    String locationName = checkedChipLocation.getText().toString();
                    Location reminderLocation = Location.getLocationObjectByName(locationName);
                    Location oldLocation = habit.getRemindAtLocation();
                    habit.setRemindAtLocation(reminderLocation);
                }

                progress.setQtyGoal(habitGoalQty);
                if (progress.getQtyCompleted() >= habitGoalQty) {
                    progress.setQtyCompleted(habitGoalQty);
                    progress.setCompleted(true);
                }

                ((MainActivity)getActivity()).setHabitList(habits);

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

        btnDeleteHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.delete_dialog_title);
                builder.setMessage(R.string.delete_dialog_message);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteHabit();
                        dialog.dismiss();
                        HomeFragment homeFragment = new HomeFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.flContainer, homeFragment, "findThisFragment")
                                .addToBackStack(null)
                                .commit();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

    }

    public void deleteHabit() {
        ParseQuery<Progress> queryProgressEntries = ParseQuery.getQuery(Progress.class);
        queryProgressEntries.whereEqualTo(Progress.KEY_USER, ParseUser.getCurrentUser());
        queryProgressEntries.whereEqualTo(Progress.KEY_HABIT, habit);
        queryProgressEntries.setLimit(1000);
        queryProgressEntries.addAscendingOrder(Progress.KEY_DATE);
        queryProgressEntries.include(Progress.KEY_USER);
        queryProgressEntries.include(Progress.KEY_HABIT);
        queryProgressEntries.findInBackground(new FindCallback<Progress>() {
            @Override
            public void done(List<Progress> progressList, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error retrieving all progress entries for this habit");
                }
                Log.d(TAG, "Successfully retrieved all progress entries for this habit");
                Map<String, Double> dateToProgress = new HashMap<>();
                for (Progress progress : progressList) {
                    dateToProgress.put(progress.getDate(), progress.getPctCompleted());
                }
                ParseQuery<OverallProgress> queryOverallProgress = ParseQuery.getQuery(OverallProgress.class);
                queryOverallProgress.whereEqualTo(Progress.KEY_USER, ParseUser.getCurrentUser());
                queryOverallProgress.setLimit(1000);
                queryOverallProgress.addAscendingOrder(Progress.KEY_DATE);
                queryOverallProgress.findInBackground(new FindCallback<OverallProgress>() {
                    @Override
                    public void done(List<OverallProgress> overallProgressList, ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error retrieving all OverallProgress entries");
                        }
                        Log.d(TAG, "Successfully retrieved all OverallProgress entries");
                        for (OverallProgress overallProgress : overallProgressList) {
                            String date = overallProgress.getDate();
                            if (dateToProgress.keySet().contains(date)) {
                                int numHabits = overallProgress.getNumHabits();
                                double totalProgress = overallProgress.getOverallPct();
                                double thisProgress = dateToProgress.get(date);
                                double updatedProgress = (totalProgress * numHabits - thisProgress) / (numHabits - 1);
                                overallProgress.setOverallPct(updatedProgress);
                                overallProgress.setNumHabits(numHabits-1);
                            }
                        }
                        ParseObject.saveAllInBackground(overallProgressList, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Error saving updated OverallProgress entries");
                                }
                                Log.d(TAG, "Successfully saved updated OverallProgress entries");
                            }
                        });
                    }
                });
                ParseObject.deleteAllInBackground(progressList, new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error deleting all progress entries for this habit");
                        }
                        Log.d(TAG, "Successfully deleted all progress entries for this habit");
                    }
                });
                try {
                    ParseObject.deleteAll(Arrays.asList(habit));
                    habits.remove(position);
                    ((MainActivity)getActivity()).setHabitList(habits);
                } catch (ParseException parseException) {
                    Log.e(TAG, "Error deleting habit");
                    parseException.printStackTrace();
                }
            }
        });

    }

}