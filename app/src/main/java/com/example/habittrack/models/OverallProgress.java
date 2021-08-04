package com.example.habittrack.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;

@ParseClassName("OverallProgress")
public class OverallProgress extends ParseObject implements Serializable {

    public static final String KEY_USER = "user";
    public static final String KEY_DATE = "date";
    public static final String KEY_NUM_HABITS = "numHabits";
    public static final String KEY_OVERALL_PCT = "overallPct";

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }

    public String getDate() { return getString(KEY_DATE); }

    public void setDate(String date) { put(KEY_DATE, date); }

    public int getNumHabits() { return getInt(KEY_NUM_HABITS); }

    public void setNumHabits(int numHabits) { put(KEY_NUM_HABITS, numHabits); }

    public double getOverallPct() { return getDouble(KEY_OVERALL_PCT); }

    public void setOverallPct(double overallPct) { put(KEY_OVERALL_PCT, overallPct); }

}
