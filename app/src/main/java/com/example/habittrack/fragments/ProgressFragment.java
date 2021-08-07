package com.example.habittrack.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.habittrack.DayViewContainer;
import com.example.habittrack.HabitWrapper;
import com.example.habittrack.MainActivity;
import com.example.habittrack.MonthHeaderViewContainer;
import com.example.habittrack.OverallProgressWrapper;
import com.example.habittrack.ProgressTabHabitsAdapter;
import com.example.habittrack.ProgressWrapper;
import com.example.habittrack.R;
import com.example.habittrack.models.Habit;
import com.example.habittrack.models.OverallProgress;
import com.example.habittrack.models.Progress;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.MaterialFadeThrough;
import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ObjIntConsumer;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProgressFragment extends Fragment {

    public static final String TAG = "ProgressFragment";
    private Context context;

    private CalendarView calendarView;
    private RecyclerView rvHabits;
    private List<Progress> progressList;
    private List<OverallProgress> overallProgressList;
    private ProgressTabHabitsAdapter adapter;
    private Map<String, Double> dateToProgressMap = new HashMap<>();

    private YearMonth currentVisibleMonth;

    public ProgressFragment() {};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        overallProgressList = ((MainActivity) getActivity()).getOverallProgressList();
        if (overallProgressList.isEmpty()) {
            Log.e(TAG, "overallProgressList is empty");
        } else {
            for (OverallProgress overallProgress : overallProgressList) {
                dateToProgressMap.put(overallProgress.getDate(), overallProgress.getOverallPct());
            }
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("habits-progress-specific-day"));
        setEnterTransition(new MaterialFadeThrough());
        setExitTransition(new MaterialFadeThrough());
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
        rvHabits = view.findViewById(R.id.rvProgressHabits);

        calendarSetup();

        progressList = new ArrayList<>();
        adapter = new ProgressTabHabitsAdapter(context, progressList);
        rvHabits.setAdapter(adapter);
        rvHabits.setLayoutManager(new LinearLayoutManager(context));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("habits-progress-specific-day")) {
                Log.d(TAG, "received local broadcast");
                ProgressWrapper wrapper = (ProgressWrapper) intent.getSerializableExtra("queried-progress-list");
                List<Progress> queriedProgressList = wrapper.getProgressList();
                progressList.clear();
                progressList.addAll(queriedProgressList);
                adapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "LocalBroadcast type not recognized");
            }
        }
    };

    private void calendarSetup() {
        calendarView.setDayBinder(new DayBinder<ViewContainer>() {
            @NotNull
            @Override
            public ViewContainer create(@NotNull View view) {
                return new DayViewContainer(view, currentVisibleMonth);
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
                    String dateString = formatLocalDate(calendarDay.getDate());
                    GradientDrawable background = (GradientDrawable) dayViewContainer.tvCalendarDay.getBackground();
                    if (dateToProgressMap.containsKey(dateString)) {
                        Double ratio = dateToProgressMap.get(dateString);
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

        calendarView.setMonthScrollListener(new Function1<CalendarMonth, Unit>() {
            @Override
            public Unit invoke(CalendarMonth calendarMonth) {
                Log.d(TAG, "calendar scrolled");
                currentVisibleMonth = calendarMonth.getYearMonth();
                calendarView.notifyMonthChanged(calendarMonth.getYearMonth());
                return null;
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