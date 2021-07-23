package com.example.habittrack.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.example.habittrack.HabitWrapper;
import com.example.habittrack.MainActivity;
import com.example.habittrack.models.Habit;
import com.example.habittrack.HabitsAdapter;
import com.example.habittrack.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    protected RecyclerView rvHabits;
    protected HabitsAdapter adapter;
    protected List<Habit> habits;

    public HomeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        rvHabits = view.findViewById(R.id.rvHabits);
        habits = ((MainActivity)getActivity()).getHabitList();
        adapter = new HabitsAdapter(getContext(), habits);
        rvHabits.setAdapter(adapter);
        rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}