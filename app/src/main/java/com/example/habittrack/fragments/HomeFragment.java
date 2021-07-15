package com.example.habittrack.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.habittrack.HabitQuery;
import com.example.habittrack.models.Habit;
import com.example.habittrack.HabitsAdapter;
import com.example.habittrack.R;
import com.example.habittrack.models.Progress;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    protected RecyclerView rvHabits;
    protected HabitsAdapter adapter;
    protected List<Habit> allHabits;
    protected List<Progress> allProgress;

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvHabits = view.findViewById(R.id.rvHabits);
        allHabits = new ArrayList<>();
        allProgress = new ArrayList<>();
        adapter = new HabitsAdapter(getContext(), allHabits, allProgress);

        rvHabits.setAdapter(adapter);
        rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));

        // HabitQuery.queryDefault(adapter, allHabits);

        // queryPosts(); // TODO: is this needed? the createdAt sort option is automatically selected on app startup, and it performs a query
    }

    protected void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Habit> query = ParseQuery.getQuery(Habit.class);
        // include data referred by user key -- Q: why is this not included by default? Is it a "shallow" query by default?
        query.include(Habit.KEY_USER); // is this a pointer to a User object?
        // only show habits of current user
        query.whereEqualTo(Habit.KEY_USER, ParseUser.getCurrentUser());
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Habit>() {
            @Override
            public void done(List<Habit> habits, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting habits", e);
                    return;
                }
                // for debugging purposes let's print every habit to logcat
                for (Habit habit : habits) {
                    Log.i(TAG, "Habit: " + habit.getName());
                }
                // save received posts to list and notify adapter of new data
                allHabits.addAll(habits); // "posts" is the List that the adapter has access to
                adapter.notifyDataSetChanged();
            }
        });
    }
}