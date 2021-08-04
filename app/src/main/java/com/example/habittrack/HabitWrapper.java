package com.example.habittrack;

import com.example.habittrack.models.Habit;

import java.io.Serializable;
import java.util.List;

public class HabitWrapper implements Serializable {
    List<Habit> habits;
    public HabitWrapper(List<Habit> h) {
        habits = h;
    }
    public List<Habit> getHabits() {
        return habits;
    }
}