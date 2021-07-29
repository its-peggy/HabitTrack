package com.example.habittrack;

import android.view.View;
import android.widget.TextView;

import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;

public class DayViewContainer extends ViewContainer {

    public final TextView tvCalendarDay;

    public DayViewContainer(View itemView) {
        super(itemView);
        tvCalendarDay = itemView.findViewById(R.id.tvCalendarDay);
    }

}
