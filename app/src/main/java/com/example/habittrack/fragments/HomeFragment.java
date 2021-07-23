package com.example.habittrack.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.habittrack.HabitWrapper;
import com.example.habittrack.MainActivity;
import com.example.habittrack.models.Habit;
import com.example.habittrack.HabitsAdapter;
import com.example.habittrack.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialArcMotion;
import com.google.android.material.transition.MaterialContainerTransform;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    protected RecyclerView rvHabits;
    protected FloatingActionButton fabNewHabit;
    protected HabitsAdapter adapter;
    protected List<Habit> habits;

   //  final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

    public HomeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        BottomNavigationView bottomNav = ((MainActivity)getActivity()).findViewById(R.id.bottomNavigation);
        bottomNav.setVisibility(View.VISIBLE);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        fabNewHabit = view.findViewById(R.id.fabNewHabit);
        rvHabits = view.findViewById(R.id.rvHabits);
        habits = ((MainActivity)getActivity()).getHabitList();
        adapter = new HabitsAdapter(getContext(), habits);
        rvHabits.setAdapter(adapter);
        rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));

        fabNewHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateFragment createFragment = new CreateFragment();

                MaterialContainerTransform transform = new MaterialContainerTransform();


                transform.setAllContainerColors(MaterialColors.getColor(v, R.attr.colorSurface));
                transform.setPathMotion(new MaterialArcMotion());
                transform.setDuration(400);
                transform.setInterpolator(new FastOutSlowInInterpolator());
                transform.setFadeMode(MaterialContainerTransform.FADE_MODE_CROSS);

                createFragment.setSharedElementEnterTransition(transform);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContainer, createFragment, "findThisFragment")
                        .addToBackStack(null)
                        .addSharedElement(fabNewHabit, "shared_element_container")
                        .commit();
            }
        });
    }
}