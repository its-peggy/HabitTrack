package com.example.habittrack.fragments;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.habittrack.DayViewContainer;
import com.example.habittrack.MainActivity;
import com.example.habittrack.MonthHeaderViewContainer;
import com.example.habittrack.R;
import com.example.habittrack.StartActivity;
import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Progress;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgressFragment extends Fragment {

    public static final String TAG = "ProgressFragment";
    private Context context;

    private CalendarView calendarView;

    public ProgressFragment() {};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BottomNavigationView bottomNav = ((MainActivity)getActivity()).findViewById(R.id.bottomNavigation);
        bottomNav.setVisibility(View.VISIBLE);
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        calendarView = view.findViewById(R.id.calendarView);

        ParseQuery<Progress> queryProgressEntries = ParseQuery.getQuery(Progress.class);
        queryProgressEntries.whereEqualTo(Progress.KEY_USER, ParseUser.getCurrentUser());
        queryProgressEntries.include(Progress.KEY_USER);
        queryProgressEntries.include(Progress.KEY_HABIT);
        queryProgressEntries.findInBackground(new FindCallback<Progress>() {
            @Override
            public void done(List<Progress> progressList, ParseException e) {
                for (Progress progress : progressList) {
                    String date = progress.getDate();
                    if (!Progress.dateToTotalProgressPct.containsKey(date)) {
                        Progress.dateToTotalProgressPct.put(date, progress.getPctCompleted() / 100);
                    } else {
                        double currentTotalPct = Progress.dateToTotalProgressPct.get(date);
                        Progress.dateToTotalProgressPct.put(date, currentTotalPct + progress.getPctCompleted() / 100);
                    }
                    if (!Progress.dateToProgressCount.containsKey(date)) {
                        Progress.dateToProgressCount.put(date, (long) 1);
                    } else {
                        long currentTotalCount = Progress.dateToProgressCount.get(date);
                        Progress.dateToProgressCount.put(date, currentTotalCount + 1);
                    }
                }
                Map<String, Double> dateToPct = new HashMap<>();
                for (Map.Entry<String, Double> entry : Progress.dateToTotalProgressPct.entrySet()) {
                    String date = entry.getKey();
                    Double totalPct = entry.getValue();
                    Long totalCount = Progress.dateToProgressCount.get(date);
                    dateToPct.put(date, totalPct / totalCount);
                }
                calendarSetup(dateToPct);
            }
        });

    }

    private void calendarSetup(Map<String, Double> dateToPct) {
        calendarView.setDayBinder(new DayBinder<ViewContainer>() {
            @NotNull
            @Override
            public ViewContainer create(@NotNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NotNull ViewContainer viewContainer, @NotNull CalendarDay calendarDay) {
                DayViewContainer dayViewContainer = (DayViewContainer) viewContainer;
                dayViewContainer.tvCalendarDay.setText(Integer.toString(calendarDay.getDate().getDayOfMonth()));
                if (calendarDay.getOwner() != DayOwner.THIS_MONTH) {
                    dayViewContainer.tvCalendarDay.setTextColor(Color.LTGRAY);
                    GradientDrawable background = (GradientDrawable) dayViewContainer.tvCalendarDay.getBackground();
                    background.setColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    dayViewContainer.tvCalendarDay.setTextColor(Color.BLACK);
                    LocalDate localDate = calendarDay.getDate();
                    String dateString = formatLocalDate(localDate);
                    GradientDrawable background = (GradientDrawable) dayViewContainer.tvCalendarDay.getBackground();
                    if (dateToPct.containsKey(dateString)) {
                        Double ratio = dateToPct.get(dateString);
                        int septile = (int) Math.floor(ratio * 7);
                        int color = 0;
                        switch (septile) {
                            case 0:
                                color = ContextCompat.getColor(context, R.color.progress0);
                                break;
                            case 1:
                                color = ContextCompat.getColor(context, R.color.progress1);
                                break;
                            case 2:
                                color = ContextCompat.getColor(context, R.color.progress2);
                                break;
                            case 3:
                                color = ContextCompat.getColor(context, R.color.progress3);
                                break;
                            case 4:
                                color = ContextCompat.getColor(context, R.color.progress4);
                                break;
                            case 5:
                                color = ContextCompat.getColor(context, R.color.progress5);
                                break;
                            case 6:
                                color = ContextCompat.getColor(context, R.color.progress6);
                                break;
                        }
                        background.setColor(color);
                    } else {
                        background.setColor(ContextCompat.getColor(context, R.color.very_light_grey));
                    }
                }
            }
        });

        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<ViewContainer>() {
            @NotNull
            @Override
            public ViewContainer create(@NotNull View view) {
                return new MonthHeaderViewContainer(view);
            }

            @Override
            public void bind(@NotNull ViewContainer viewContainer, @NotNull CalendarMonth calendarMonth) {
                Month month = Month.of(calendarMonth.getMonth());
                String monthString = month.getDisplayName(TextStyle.FULL, Locale.getDefault());
                String year = String.valueOf(calendarMonth.getYear());
                String displayHeader = monthString + " " + year;

                MonthHeaderViewContainer monthHeaderViewContainer = (MonthHeaderViewContainer) viewContainer;
                monthHeaderViewContainer.tvCalendarMonthHeader.setText(displayHeader);
            }
        });

        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(12);
        YearMonth lastMonth = currentMonth.plusMonths(1);
        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        calendarView.setup(firstMonth, lastMonth, firstDayOfWeek);
        calendarView.scrollToMonth(currentMonth);
    }

    private String formatLocalDate(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayFormatted = formatter.format(localDate);
        return todayFormatted;
    }

}