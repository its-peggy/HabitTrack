package com.example.habittrack;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.habittrack.fragments.HabitDetailFragment;
import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "HabitsAdapter";

    private static final int HEADER_VIEW = 100;
    private static final int SECTION_HEADER_VIEW = 2;
    private Context context;
    private static Map<Habit, Progress> habitProgressMap = new HashMap<>();
    private List<Habit> habits;
    // private List<Progress> progresses; // TODO: is this actually needed?

    private static final List<String> TIME_OF_DAY_SECTIONS = Arrays.asList("All day", "Morning", "Noon", "Afternoon", "Evening", "Night");
    private static final List<String> TAG_SECTIONS = Arrays.asList("Education", "Exercise", "Health", "Personal", "Productivity");
    private static final List<String> STATUS_SECTIONS = Arrays.asList("Not completed", "Completed");

    private static final int NUM_HEADERS_CREATION_DATE = 0;
    private static final int NUM_HEADERS_TIME_OF_DAY = TIME_OF_DAY_SECTIONS.size();
    private static final int NUM_HEADERS_TAG = TAG_SECTIONS.size();
    private static final int NUM_HEADERS_STATUS = STATUS_SECTIONS.size();

    private static int LAST_SORT_SELECTED = 0;

    private static final Map<Integer, Integer> sortTypeToNumSections;
    private static final Map<Integer, List<String>> sortTypeToSectionNames;
    private static Map<Integer, String> sectionHeaderPositionToName = new HashMap<>();
    private static Map<Integer, Integer> habitPositionToOriginal = new HashMap<>();

    static {
        sortTypeToNumSections = new HashMap<>();
        sortTypeToNumSections.put(0, NUM_HEADERS_CREATION_DATE);
        sortTypeToNumSections.put(1, NUM_HEADERS_TIME_OF_DAY);
        sortTypeToNumSections.put(2, NUM_HEADERS_TAG);
        sortTypeToNumSections.put(3, NUM_HEADERS_STATUS);

        sortTypeToSectionNames = new HashMap<>();
        sortTypeToSectionNames.put(1, TIME_OF_DAY_SECTIONS);
        sortTypeToSectionNames.put(2, TAG_SECTIONS);
        sortTypeToSectionNames.put(3, STATUS_SECTIONS);
    }

    public HabitsAdapter(Context context, List<Habit> habits) {
        this.context = context;
        this.habits = habits;
        // this.progresses = progresses;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == HEADER_VIEW) {
            view = LayoutInflater.from(context).inflate(R.layout.item_home_header, parent, false);
            return new HeaderViewHolder(view, LAST_SORT_SELECTED);
        }
        else if (viewType == SECTION_HEADER_VIEW) {
            view = LayoutInflater.from(context).inflate(R.layout.item_home_section_header, parent, false);
            return new SectionHeaderViewHolder(view);
        }
        else {
            view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false);
            return new HabitViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            if (holder instanceof HeaderViewHolder) {
                HeaderViewHolder vh = (HeaderViewHolder) holder;
                vh.bind();
            } else if (holder instanceof HabitViewHolder) {
                HabitViewHolder vh = (HabitViewHolder) holder;
                int offset = getHabitOffset(position);
                if (offset == -1) {
                    Log.e(TAG, "error getting habit offset");
                }
                vh.bind(habits.get(position-offset));
            } else if (holder instanceof SectionHeaderViewHolder) {
                SectionHeaderViewHolder vh = (SectionHeaderViewHolder) holder;
                vh.bind(sectionHeaderPositionToName.get(position));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getHabitOffset(int position) {
        if (sectionHeaderPositionToName.isEmpty()) { // no section headers (sorting by createdAt)
            int offset = 1;
            habitPositionToOriginal.put(position, position-offset);
            return offset;
        }
        List<Integer> sectionHeaderPositions = new ArrayList<Integer>(sectionHeaderPositionToName.keySet());
        Collections.sort(sectionHeaderPositions);
        int listSize = sectionHeaderPositions.size();
        for (int i = 0; i < listSize - 1; i++) {
            if (sectionHeaderPositions.get(i) < position && position < sectionHeaderPositions.get(i+1)) {
                int offset = i + 2;
                habitPositionToOriginal.put(position, position-offset);
                return offset;
            }
        }
        if (sectionHeaderPositions.get(listSize-1) < position) {
            int offset = listSize + 1;
            habitPositionToOriginal.put(position, position-offset);
            return offset;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        if (habits != null) {
            return 1 + sortTypeToNumSections.get(LAST_SORT_SELECTED) + habits.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER_VIEW;
        }
        if (sectionHeaderPositionToName.containsKey(position)) {
            return SECTION_HEADER_VIEW;
        }
        return super.getItemViewType(position);
    }

    class HabitViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivIcon;
        private TextView tvHabitName;
        private TextView tvAmount;
        private TextView tvTimeOfDay;
        private TextView tvRemind;
        private TextView tvTag;

        private LayoutInflater layoutInflater;
        private PopupWindow popupWindow;

        private Habit mHabit;
        private TextView tvPopupHabitName;
        private SeekBar sbProgress;
        private TextView tvSeekBarMin;
        private TextView tvSeekBarMax;
        private TextView tvPopupProgress;
        private Button btnSaveProgress;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvTimeOfDay = itemView.findViewById(R.id.tvTimeOfDay);
            tvRemind = itemView.findViewById(R.id.tvRemind);
            tvTag = itemView.findViewById(R.id.tvTag);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    HabitDetailFragment habitDetailFragment = new HabitDetailFragment();
                    Bundle bundle = new Bundle();
                    int originalPosition = habitPositionToOriginal.get(position);
                    Habit habit = habits.get(originalPosition);
                    // bundle.putSerializable("Habit", habit);
                    bundle.putSerializable("Habit", new HabitWrapper(habits));
                    bundle.putInt("Position", originalPosition);
                    habitDetailFragment.setArguments(bundle);
                    AppCompatActivity activity = (AppCompatActivity) itemView.getContext();
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.flContainer, habitDetailFragment, "findThisFragment")
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
            });
        }

        public void bind(Habit habit) {
            mHabit = habit;
            ParseFile icon = habit.getIcon();
            if (icon != null) {
                Glide.with(context)
                        .load(icon.getUrl())
                        .transform(new CircleCrop())
                        .into(ivIcon);
            }
            tvHabitName.setText(habit.getName());
            String qtyCompletedToday = String.valueOf(habit.getTodayProgress().getQtyCompleted());
            String fractionDone = qtyCompletedToday + "/" + habit.getQtyGoal() + " " + habit.getUnit();
            tvAmount.setText(fractionDone);
            tvTimeOfDay.setText(habit.getTimeOfDay());
            if (habit.getRemindAtLocation() != null) {
                // TODO: change icon to map pin if location reminder
                tvRemind.setText(habit.getRemindAtLocation().getName());
            }
            else {
                Date remindTime = habit.getRemindAtTime();
                LocalDateTime localDateTime = convertToLocalDateTime(remindTime);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
                tvRemind.setText(formatter.format(localDateTime));
            }
            tvTag.setText(habit.getTag());
        }

        @Override
        public void onClick(View v) {
            int habitPosition = this.getLayoutPosition();

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup_progress_window, null);

            tvPopupHabitName = container.findViewById(R.id.tvPopupHabitName);
            sbProgress = container.findViewById(R.id.sbProgress);
            tvSeekBarMin = container.findViewById(R.id.tvSeekBarMin);
            tvSeekBarMax = container.findViewById(R.id.tvSeekBarMax);
            tvPopupProgress = container.findViewById(R.id.tvPopupProgress);
            btnSaveProgress = container.findViewById(R.id.btnSaveProgress);

            int seekBarMin = 0;
            int seekBarMax = mHabit.getQtyGoal();
            int currentProgress = mHabit.getTodayProgress().getQtyCompleted();
            int goalQty = mHabit.getQtyGoal();
            String unit = mHabit.getUnit();

            tvPopupHabitName.setText(mHabit.getName());
            sbProgress.setMax(seekBarMax);
            sbProgress.setProgress(currentProgress);
            tvSeekBarMin.setText(String.valueOf(seekBarMin));
            tvSeekBarMax.setText(String.valueOf(seekBarMax));
            String progress = currentProgress + "/" + goalQty + " " + unit;
            tvPopupProgress.setText(progress);

            sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String updatedProgress = progress + "/" + goalQty + " " + unit;
                    tvPopupProgress.setText(updatedProgress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });

            popupWindow = new PopupWindow(container, 600, 400, true);
            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

            btnSaveProgress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int newProgress = sbProgress.getProgress();
                    Progress progress = mHabit.getTodayProgress();
                    int goal = progress.getQtyGoal();
                    progress.setQtyCompleted(newProgress);
                    progress.setPctCompleted(100 * (double) newProgress / goal);
                    progress.setCompleted(newProgress == goal);
                    progress.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d("seekbar", "progress change saved");
                            notifyItemChanged(habitPosition);
                        }
                    });
                    popupWindow.dismiss();
                }
            });

        }

    }

    protected void makeSectionHeaderPositionToName(List<Habit> habits, int sortType) {
        if (sortType == 0) {
            sectionHeaderPositionToName.clear();
            return;
        }
        List<String> labels = sortTypeToSectionNames.get(sortType);
        Map<String, Integer> numHabitsOfEachType = new HashMap<>();
        for (String label : labels) {
            numHabitsOfEachType.put(label, 0);
        }
        for (Habit habit : habits) {
            String key;
            if (sortType == 1) {
                key = habit.getTimeOfDay();
            }
            else if (sortType == 2) {
                key = habit.getTag();
            }
            else {
                assert (sortType == 3);
                key = habit.getTodayProgress().getCompleted() ? "Completed" : "Not completed";
            }
            numHabitsOfEachType.put(key, numHabitsOfEachType.get(key) + 1);
        }
        sectionHeaderPositionToName.clear();
        int current = 1;
        for (int i = 0; i < labels.size(); i++) {
            sectionHeaderPositionToName.put(current, labels.get(i));
            current += numHabitsOfEachType.get(labels.get(i)) + 1;
        }
    }

    protected void queryDatabase(int sortType) {
        Log.d(TAG, "querying database");
        ParseQuery<Habit> queryHabits = ParseQuery.getQuery(Habit.class);
        queryHabits.include(Habit.KEY_USER);
        queryHabits.include(Habit.KEY_TODAY_PROGRESS);
        if (sortType != 0) {
            Log.e(TAG, "upon Parse query, sortType not default \"createdAt\" sort");
            return;
        }
        queryHabits.addAscendingOrder(Habit.KEY_CREATED_AT);
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
                // progresses.clear();
                habitProgressMap.clear();
                for (Habit habit : habits) {
                    Progress progress = habit.getTodayProgress();
//                        Log.i(TAG, "Habit: " + habit.getName()
//                                + " Progress: " + progress.getDate() + " is " + progress.getQtyCompleted() + " of " + progress.getQtyGoal());
//                        Log.i(TAG, "Creation Date: " + habit.getCreatedAt().getClass());
                    // progresses.add(progress);
                    habitProgressMap.put(habit, progress);
                }
                makeSectionHeaderPositionToName(habits, sortType);
                notifyDataSetChanged();
            }
        });
    }

    protected void sortExistingHabits(int sortType) {
        switch(sortType) {
            case 0:
                Collections.sort(habits, new Habit.CreationDateComparator());
                break;
            case 1:
                Collections.sort(habits, new Habit.TimeOfDayComparator());
                break;
            case 2:
                Collections.sort(habits, new Habit.TagComparator());
                break;
            case 3:
                Collections.sort(habits, new Habit.StatusComparator());
                break;
        }
        makeSectionHeaderPositionToName(habits, sortType);
//        progresses.clear();
//        for (Habit habit : habits) {
//            progresses.add(habitProgressMap.get(habit));
//        }
        notifyDataSetChanged();
    }



    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvHeaderDate;
        private Spinner spSort;

        public HeaderViewHolder(@NonNull View itemView, int sortTypePosition) {
            super(itemView);
            tvHeaderDate = itemView.findViewById(R.id.tvHeaderDate);
            spSort = itemView.findViewById(R.id.spSort);

            ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(context,
                    R.array.sort_spinner_array, android.R.layout.simple_spinner_item);
            sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSort.setAdapter(sortAdapter);
            spSort.setSelection(sortTypePosition);
            spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "spinner onItemSelected called");
                    LAST_SORT_SELECTED = position;
                    if (habits.isEmpty()) {
                        queryDatabase(LAST_SORT_SELECTED);
                    } else {
                        sortExistingHabits(LAST_SORT_SELECTED);
                    }
                    // queryWithSort(LAST_SORT_SELECTED); // 0-3, corresponds to sort type
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO: what goes here?
                }
            });

        }

        public void bind() {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            tvHeaderDate.setText(formatter.format(now));
        }

    }

    class SectionHeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvSectionName;

        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionName = itemView.findViewById(R.id.tvSectionName);
        }

        public void bind(String sectionName) {
            tvSectionName.setText(sectionName);
        }
    }

    public LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

}
