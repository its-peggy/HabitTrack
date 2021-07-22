package com.example.habittrack.fragments;

import android.content.Intent;
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
import android.widget.Toast;

import com.example.habittrack.HabitWrapper;
import com.example.habittrack.ProgressWrapper;
import com.example.habittrack.models.Habit;
import com.example.habittrack.HabitsAdapter;
import com.example.habittrack.R;
import com.example.habittrack.models.Progress;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    protected RecyclerView rvHabits;
    protected HabitsAdapter adapter;
    protected List<Habit> habits;
    // protected List<Progress> progresses;

    public HomeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvHabits = view.findViewById(R.id.rvHabits);

        Bundle bundle = getArguments();
        if (bundle != null) {
            HabitWrapper hw = (HabitWrapper) bundle.getSerializable("Habit");
            // ProgressWrapper pw = (ProgressWrapper) bundle.getSerializable("Progress");
            habits = hw.getHabits();
            // progresses = pw.getProgress();
        }

        adapter = new HabitsAdapter(getContext(), habits);

        rvHabits.setAdapter(adapter);
        rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));
    }

}