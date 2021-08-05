package com.example.habittrack;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;
import com.parse.ParseFile;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProgressTabHabitsAdapter extends RecyclerView.Adapter<ProgressTabHabitsAdapter.ViewHolder> {

    public static final String TAG = "ProgressTabHabitsAdapter";

    private Context context;
    private List<Progress> progressList;

    public ProgressTabHabitsAdapter(Context context, List<Progress> progressList) {
        this.context = context;
        this.progressList = progressList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_progress_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Progress progress = progressList.get(position);
        holder.bind(progress);
    }

    @Override
    public int getItemCount() {
        return progressList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivIcon;
        private TextView tvHabitName;
        private TextView tvAmount;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon_ProgressRV);
            tvHabitName = itemView.findViewById(R.id.tvHabitName_ProgressRV);
            tvAmount = itemView.findViewById(R.id.tvProgressAmt_ProgressRV);
        }

        public void bind(Progress progress) {
            Habit habit = progress.getHabit();
            ParseFile icon = habit.getIcon();
            if (icon != null) {
                Glide.with(context)
                        .load(icon.getUrl())
                        .transform(new CenterCrop())
                        .into(ivIcon);
            }
            tvHabitName.setText(habit.getName());
            String progressString = progress.getQtyCompleted() + "/" + progress.getQtyGoal() + " " + habit.getUnit();
            tvAmount.setText(progressString);
        }
    }
}
