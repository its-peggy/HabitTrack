package com.example.habittrack.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ParseClassName("Progress")
public class Progress extends ParseObject {

    public static final String KEY_USER = "user";
    public static final String KEY_HABIT = "habit";
    public static final String KEY_DATE = "date";
    public static final String KEY_QTY_COMPLETED = "qtyCompleted";
    public static final String KEY_QTY_GOAL = "qtyGoal";
    public static final String KEY_PCT_COMPLETED = "pctCompleted";
    public static final String KEY_COMPLETED = "completed";

    public ParseUser getUser() { return getParseUser(KEY_USER); }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }

    public Habit getHabit() { return (Habit) get(KEY_HABIT); }

    public void setHabit(Habit habit) { put(KEY_HABIT, habit); }

    public String getDate() { return getString(KEY_DATE); }

    public void setDate(String date) { put(KEY_DATE, date); }

    public int getQtyCompleted() { return getInt(KEY_QTY_COMPLETED); }

    public void setQtyCompleted(int qtyCompleted) { put(KEY_QTY_COMPLETED, qtyCompleted); }

    public int getQtyGoal() { return getInt(KEY_QTY_GOAL); }

    public void setQtyGoal(int qtyGoal) { put(KEY_QTY_GOAL, qtyGoal); }

    public double getPctCompleted() { return getDouble(KEY_PCT_COMPLETED); }

    public void setPctCompleted(double pctCompleted) { put(KEY_PCT_COMPLETED, pctCompleted); }

    public boolean getCompleted() { return getBoolean(KEY_COMPLETED); }

    public void setCompleted(boolean completed) { put(KEY_COMPLETED, completed); }

    public static String getTodayDateString() {
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = localDate.format(formatter);
        return dateString;
    }
}
