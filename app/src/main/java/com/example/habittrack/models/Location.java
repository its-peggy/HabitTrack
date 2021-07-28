package com.example.habittrack.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

@ParseClassName("Location")
public class Location extends ParseObject {

    public static final String KEY_USER = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_LOCATION = "location";

    public static Map<String, Location> nameToLocationObject = new HashMap<>();

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

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(KEY_LOCATION);
    }

    public void setLocation(ParseGeoPoint location) {
        put(KEY_LOCATION, location);
    }

    public static Location getLocationObjectByName(String locationName) {
        return nameToLocationObject.get(locationName);
    }

}
