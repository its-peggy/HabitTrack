package com.example.habittrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.parse.ParseFile;

import java.util.List;

public class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.ViewHolder> {

    private Context context;
    private List<Habit> habits;

    public HabitsAdapter(Context context, List<Habit> habits) {
        this.context = context;
        this.habits = habits;
    }

    @NonNull
    @Override
    public HabitsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitsAdapter.ViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.bind(habit);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView ivIcon;
        private TextView tvHabitName;
        private TextView tvAmount;
        private TextView tvTimeOfDay;
        private TextView tvRemind;
        private TextView tvTag;

        public ViewHolder(@NonNull View itemView) {
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
            // TODO: How to access amount done on current day... change data types from Date to String ("05/12/21")?
            String fractionDone = "x/" + habit.getQtyGoal() + " " + habit.getUnit();
            tvAmount.setText(fractionDone);
            tvTimeOfDay.setText(habit.getTimeOfDay());
            if (habit.getRemindAtLocation() != null) {
                tvRemind.setText(habit.getRemindAtLocation().getName());
            }
            else {
                tvRemind.setText(habit.getRemindAtTime().toString()); // TODO: Convert time format
            }
            tvTag.setText(habit.getTag());
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(context, "habit clicked", Toast.LENGTH_SHORT).show(); // TODO: on habit clicked
        }

    }
}
