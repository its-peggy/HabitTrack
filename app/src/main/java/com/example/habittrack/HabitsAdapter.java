package com.example.habittrack;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.habittrack.models.Habit;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import okhttp3.internal.http2.Header;

public class HabitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_VIEW = 1;
    private Context context;
    private List<Habit> habits;

    public HabitsAdapter(Context context, List<Habit> habits) {
        this.context = context;
        this.habits = habits;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == HEADER_VIEW) {
            view = LayoutInflater.from(context).inflate(R.layout.item_home_header, parent, false);
            return new HeaderViewHolder(view);
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
                vh.bind(habits.get(position-1));
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
            ParseFile icon = habit.getIcon();
            if (icon != null) {
                Glide.with(context)
                        .load(icon.getUrl())
                        .transform(new CircleCrop())
                        .into(ivIcon);
            }
            tvHabitName.setText(habit.getName());
            // TODO: How to access amount done on current day... change Progress data types from Date to String ("05/12/21")?
            String fractionDone = "x/" + habit.getQtyGoal() + " " + habit.getUnit();
            tvAmount.setText(fractionDone);
            tvTimeOfDay.setText(habit.getTimeOfDay());
            if (habit.getRemindAtLocation() != null) {
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
        }

    }

    protected void queryWithSort(int sortType) {
        ParseQuery<Habit> query = ParseQuery.getQuery(Habit.class);
        query.include(Habit.KEY_USER);
        if (sortType == 0) {
            query.addAscendingOrder(Habit.KEY_CREATED_AT);
        }
        else if (sortType == 1) {
            query.addAscendingOrder(Habit.KEY_TIME_OF_DAY_INDEX);
        }
        else {
            query.addAscendingOrder(Habit.KEY_TAG);
        }
        query.whereEqualTo(Habit.KEY_USER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Habit>() {
            @Override
            public void done(List<Habit> queriedHabits, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e("queryWithSort ", "Issue with getting habits", e);
                    return;
                }
                // debugging
                for (Habit habit : queriedHabits) {
                    Log.i("queryWithSort ", "Habit: " + habit.getName());
                }
                // save received posts to list and notify adapter of new data
                habits.clear();
                habits.addAll(queriedHabits);
                notifyDataSetChanged();
            }
        });
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvHeaderDate;
        private Spinner spSort;
        private Spinner spFilter;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderDate = itemView.findViewById(R.id.tvHeaderDate);
            spSort = itemView.findViewById(R.id.spSort);
            spFilter = itemView.findViewById(R.id.spFilter);

            ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(context,
                    R.array.sort_spinner_array, android.R.layout.simple_spinner_item);
            sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSort.setAdapter(sortAdapter);
            spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        queryWithSort(0);
                    }
                    else if (position == 1) {
                        queryWithSort(1);
                    }
                    else {
                        queryWithSort(2);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO: what goes here?
                }
            });

            ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(context,
                    R.array.filter_spinner_array, android.R.layout.simple_spinner_item);
            filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spFilter.setAdapter(filterAdapter);
            // TODO: implement item selection for filter

        }

        public void bind() {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            tvHeaderDate.setText(formatter.format(now));
        }

    }

    public LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

}
