package com.example.habittrack.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ParseClassName("Habit")
public class Habit extends ParseObject {

    public static final String KEY_USER = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_ICON = "icon";
    public static final String KEY_TODAY_PROGRESS = "todayProgress";
    public static final String KEY_TAG = "tag";
    public static final String KEY_QTY_GOAL = "qtyGoal";
    public static final String KEY_UNIT = "unit";
    public static final String KEY_TIME_OF_DAY = "timeOfDay";
    public static final String KEY_TIME_OF_DAY_INDEX = "timeOfDayIndex";
    public static final String KEY_STREAK = "streak";
    public static final String KEY_REMIND_AT_TIME = "remindAtTime";
    public static final String KEY_REMIND_AT_LOCATION = "remindAtLocation";
    public static final Map<String, Integer> TIME_OF_DAY_MAP;

    static {
        TIME_OF_DAY_MAP = new HashMap<>();
        TIME_OF_DAY_MAP.put("All day", 0);
        TIME_OF_DAY_MAP.put("Morning", 1);
        TIME_OF_DAY_MAP.put("Noon", 2);
        TIME_OF_DAY_MAP.put("Afternoon", 3);
        TIME_OF_DAY_MAP.put("Evening", 4);
        TIME_OF_DAY_MAP.put("Night", 5);
    }

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

    public Progress getTodayProgress() {
        return (Progress) get(KEY_TODAY_PROGRESS);
    }

    public void setTodayProgress(Progress progress) {
        put(KEY_TODAY_PROGRESS, progress);
    }

    public String getTag() { return getString(KEY_TAG); }

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
        put(KEY_TIME_OF_DAY_INDEX, TIME_OF_DAY_MAP.get(timeOfDay));
    }

    public int getTimeOfDayIndex() {
        return getInt(KEY_TIME_OF_DAY_INDEX);
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

    public static class CreationDateComparator implements Comparator<Habit> {
        @Override
        public int compare(Habit habit1, Habit habit2) {
            Date date1 = habit1.getCreatedAt();
            Date date2 = habit2.getCreatedAt();
            return date1.compareTo(date2);
        }
    }

    public static class TimeOfDayComparator implements Comparator<Habit> {
        @Override
        public int compare(Habit habit1, Habit habit2) {
            return habit1.getTimeOfDayIndex() - habit2.getTimeOfDayIndex();
        }
    }

    public static class TagComparator implements Comparator<Habit> {
        @Override
        public int compare(Habit habit1, Habit habit2) {
            String tag1 = habit1.getTag();
            String tag2 = habit2.getTag();
            return tag1.compareTo(tag2);
        }
    }

    public static class StatusComparator implements Comparator<Habit> {
        @Override
        public int compare(Habit habit1, Habit habit2) {
            Boolean status1 = habit1.getTodayProgress().getCompleted();
            Boolean status2 = habit2.getTodayProgress().getCompleted();
            return Boolean.compare(status1, status2);
        }
    }

}


