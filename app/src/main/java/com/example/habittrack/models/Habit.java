package com.example.habittrack.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Habit")
public class Habit extends ParseObject {

    public static final String KEY_USER = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_ICON = "icon";
    public static final String KEY_TAG = "tag";
    public static final String KEY_QTY_GOAL = "qtyGoal";
    public static final String KEY_UNIT = "unit";
    public static final String KEY_TIME_OF_DAY = "timeOfDay";
    public static final String KEY_STREAK = "streak";
    public static final String KEY_REMIND_AT_TIME = "remindAtTime";
    public static final String KEY_REMIND_AT_LOCATION = "remindAtLocation";

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public ParseFile getIcon() {
        return getParseFile(KEY_ICON);
    }

    public void setIcon(ParseFile icon) {
        put(KEY_ICON, icon);
    }

    public String getTag() {
        return getString(KEY_TAG);
    }

    public void setTag(String tag) {
        put(KEY_TAG, tag);
    }

    public int getQtyGoal() {
        return getInt(KEY_QTY_GOAL);
    }

    public void setQtyGoal(int qtyGoal) {
        put(KEY_QTY_GOAL, qtyGoal);
    }

    public String getUnit() {
        return getString(KEY_UNIT);
    }

    public void setUnit(String unit) {
        put(KEY_UNIT, unit);
    }

    public String getTimeOfDay() {
        return getString(KEY_TIME_OF_DAY);
    }

    public void setTimeOfDay(String timeOfDay) {
        put(KEY_TIME_OF_DAY, timeOfDay);
    }

    public int getStreak() {
        return getInt(KEY_STREAK);
    }

    public void setStreak(int streak) {
        put(KEY_STREAK, streak);
    }

    public Date getRemindAtTime() {
        return getDate(KEY_REMIND_AT_TIME);
    }

    public void setRemindAtTime(Date time) {
        put(KEY_REMIND_AT_TIME, time);
    }

    public Location getRemindAtLocation() {
        return (Location) get(KEY_REMIND_AT_LOCATION);
    }

    public void setRemindAtLocation(Location location) {
        put(KEY_REMIND_AT_LOCATION, location);
    }

}
