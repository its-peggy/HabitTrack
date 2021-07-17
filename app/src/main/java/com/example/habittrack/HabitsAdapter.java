package com.example.habittrack;

import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import okhttp3.internal.http2.Header;

public class HabitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_VIEW = 1;
    private static final int SECTION_HEADER_VIEW = 2;
    private Context context;
    private Map<Habit, Progress> habitProgressMap = new HashMap<>();
    private List<Habit> habits;
    private List<Progress> progresses;

    public static int TYPE_OF_SORT;

    public HabitsAdapter(Context context, List<Habit> habits, List<Progress> progresses) {
        this.context = context;
        this.habits = habits;
        this.progresses = progresses;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == HEADER_VIEW) {
            view = LayoutInflater.from(context).inflate(R.layout.item_home_header, parent, false);
            return new HeaderViewHolder(view);
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
                // Log.d("onBindViewHolder", "habits size " + habits.size() + " progresses size " + progresses.size());
                vh.bind(habits.get(position-1));
            } else if (holder instanceof SectionHeaderViewHolder) {
                SectionHeaderViewHolder vh = (SectionHeaderViewHolder) holder;
                vh.bind();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (habits != null) {
            return habits.size() + 1;
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER_VIEW;
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
            Toast.makeText(context, "habit clicked", Toast.LENGTH_SHORT).show(); // TODO: on habit clicked

            int habitPosition = this.getLayoutPosition();

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup_window, null);

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
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //Toast.makeText(context, "seekbar touch started", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //Toast.makeText(context, "seekbar touch stopped", Toast.LENGTH_SHORT).show();

                }
            });

            popupWindow = new PopupWindow(container, 400, 400, true);
            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

            btnSaveProgress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int newProgress = sbProgress.getProgress();
                    Progress progress = mHabit.getTodayProgress();
                    int goal = progress.getQtyGoal();
                    progress.setQtyCompleted(newProgress);
                    progress.setPctCompleted(100 * (double) newProgress / goal);
                    if (newProgress == goal) {
                        progress.setCompleted(true);
                    }
                    progress.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d("seekbar", "progress change saved");

                            // notifyDataSetChanged();
                            notifyItemChanged(habitPosition);
                        }
                    });
                    popupWindow.dismiss();
                }
            });

        }

    }

    public String getTodayDateString() {
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = localDate.format(formatter);
        return dateString;
    }

    protected void queryWithSort(int sortType) {
        Log.d("queryWithSort", "method called");
        if (habits.isEmpty()) {
            ParseQuery<Habit> queryHabits = ParseQuery.getQuery(Habit.class);
            queryHabits.include(Habit.KEY_USER);
            queryHabits.include(Habit.KEY_TODAY_PROGRESS);
            if (sortType != 0) {
                Log.e("queryWithSort", "sortType not default createdAt sort");
                return;
            }
            queryHabits.addAscendingOrder(Habit.KEY_CREATED_AT);
            queryHabits.whereEqualTo(Habit.KEY_USER, ParseUser.getCurrentUser());
            queryHabits.findInBackground(new FindCallback<Habit>() {
                @Override
                public void done(List<Habit> queriedHabits, ParseException e) {
                    // check for errors
                    if (e != null) {
                        Log.e("queryWithSort ", "Issue with getting habits", e);
                        return;
                    }
                    habits.clear();
                    habits.addAll(queriedHabits);
                    progresses.clear();
                    habitProgressMap.clear();
                    for (Habit habit : habits) {
                        Progress progress = habit.getTodayProgress();
                        Log.i("queryWithSort", "Habit: " + habit.getName()
                                + " Progress: " + progress.getDate() + " is " + progress.getQtyCompleted() + " of " + progress.getQtyGoal());
                        Log.i("queryWithSort", "Creation Date: " + habit.getCreatedAt().getClass());
                        progresses.add(progress);
                        habitProgressMap.put(habit, progress);
                    }
                    notifyDataSetChanged();
                }
            });
        }
        else {
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
            progresses.clear();
            for (Habit habit : habits) {
                progresses.add(habitProgressMap.get(habit));
            }
            notifyDataSetChanged();
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvHeaderDate;
        private Spinner spSort;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderDate = itemView.findViewById(R.id.tvHeaderDate);
            spSort = itemView.findViewById(R.id.spSort);

            ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(context,
                    R.array.sort_spinner_array, android.R.layout.simple_spinner_item);
            sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSort.setAdapter(sortAdapter);
            spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    queryWithSort(position); // 0-3, corresponds to sort type
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

        public void bind() {
            tvSectionName.setText("SECTION HEADER TEST");
        }
    }

    public LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

}
