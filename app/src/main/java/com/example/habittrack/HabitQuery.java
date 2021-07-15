// ignore for now

package com.example.habittrack;

import android.util.Log;

import com.example.habittrack.models.Habit;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class HabitQuery {

    public static final String TAG = "Query";

    public static void queryDefault(HabitsAdapter adapter, List<Habit> habits) {
        // specify what type of data we want to query - Post.class
        ParseQuery<Habit> query = ParseQuery.getQuery(Habit.class);
        // include data referred by user key -- Q: why is this not included by default? Is it a "shallow" query by default?
        query.include(Habit.KEY_USER); // is this a pointer to a User object?
        // TODO: order habits alphabetically?
        // only show habits of current user
        query.whereEqualTo(Habit.KEY_USER, ParseUser.getCurrentUser());
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Habit>() {
            @Override
            public void done(List<Habit> queriedHabits, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting habits", e);
                    return;
                }
                // for debugging purposes let's print every habit to logcat
                for (Habit habit : queriedHabits) {
                    Log.i(TAG, "Habit: " + habit.getName());
                }
                // save received posts to list and notify adapter of new data
                habits.clear();
                habits.addAll(queriedHabits); // "posts" is the List that the adapter has access to
                adapter.notifyDataSetChanged();
            }
        });
    }
}
