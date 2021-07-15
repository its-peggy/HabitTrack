package com.example.habittrack;

import android.app.Application;

import com.example.habittrack.models.Habit;
import com.example.habittrack.models.Location;
import com.example.habittrack.models.Progress;
import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Habit.class);
        ParseObject.registerSubclass(Location.class);
        ParseObject.registerSubclass(Progress.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("OtJFGklu8KmBLByaDS14xODbTugfw3L51XeEbtZX")
                .clientKey("0EqsWvOYBgHAerYMfmDaub6jPrXqL3ZZ5zcJgk7z")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}