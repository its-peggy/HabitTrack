package com.example.habittrack;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.habittrack.models.Progress;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

public class DayViewContainer extends ViewContainer {

    public static final String TAG = "DayViewContainer";

    public final TextView tvCalendarDay;

    public DayViewContainer(View itemView, YearMonth currentVisibleMonth) {
        super(itemView);
        tvCalendarDay = itemView.findViewById(R.id.tvCalendarDay);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String year = String.valueOf(currentVisibleMonth.getYear());
                String month = String.format("%02d", currentVisibleMonth.getMonth().getValue());
                String day = String.format("%02d", Integer.parseInt(tvCalendarDay.getText().toString()));
                String formattedDate = year + "-" + month + "-" + day;

                ParseQuery<Progress> queryProgressEntries = ParseQuery.getQuery(Progress.class);
                queryProgressEntries.whereEqualTo(Progress.KEY_USER, ParseUser.getCurrentUser());
                queryProgressEntries.whereEqualTo(Progress.KEY_DATE, formattedDate);
                queryProgressEntries.setLimit(1000);
                queryProgressEntries.include(Progress.KEY_USER);
                queryProgressEntries.include(Progress.KEY_HABIT);
                queryProgressEntries.findInBackground(new FindCallback<Progress>() {
                    @Override
                    public void done(List<Progress> progressList, ParseException e) {
                        Log.d(TAG, "queried progress size " + progressList.size() + " date " + formattedDate);
                        Intent intent = new Intent("habits-progress-specific-day");
                        ProgressWrapper wrapper = new ProgressWrapper(progressList);
                        intent.putExtra("queried-progress-list", wrapper);
                        LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
                    }
                });
            }
        });
    }

}
