package com.example.habittrack;

import android.view.View;
import android.widget.TextView;

import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;

public class MonthHeaderViewContainer extends ViewContainer {

    public final TextView tvCalendarMonthHeader;

    public MonthHeaderViewContainer(View itemView) {
        super(itemView);
        tvCalendarMonthHeader = itemView.findViewById(R.id.tvCalendarMonthHeader);
    }

}
