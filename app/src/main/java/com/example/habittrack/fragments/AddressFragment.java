package com.example.habittrack.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.habittrack.MainActivity;
import com.example.habittrack.R;
import com.example.habittrack.models.Location;
import com.example.habittrack.models.Progress;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.Headers;

public class AddressFragment extends Fragment {

    public static final String TAG = "AddressFragment";
    private Context context;

    private AsyncHttpClient asyncHttpClient;
    public static final String BASE_REQUEST_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/";
    public static final String API_KEY = "";

    private EditText etAddressName;
    private EditText etAddress;
    private EditText etCity;
    private EditText etState;
    private EditText etZipCode;
    private Button btnSaveAddress;

    public AddressFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        asyncHttpClient = new AsyncHttpClient();

        BottomNavigationView bottomNavBar = getActivity().findViewById(R.id.bottomNavigation);
        bottomNavBar.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etAddressName = view.findViewById(R.id.etAddressName);
        etAddress = view.findViewById(R.id.etAddress);
        etCity = view.findViewById(R.id.etCity);
        etState = view.findViewById(R.id.etState);
        etZipCode = view.findViewById(R.id.etZipCode);
        btnSaveAddress = view.findViewById(R.id.btnSaveAddress);

        btnSaveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addressName = etAddressName.getText().toString();
                String address = etAddress.getText().toString();
                String city = etCity.getText().toString();
                String state = etState.getText().toString();
                int zipCode = Integer.parseInt(etZipCode.getText().toString());

                String[] addressComponents = {address, city, state, Integer.toString(zipCode)};
                String fullAddress = String.join(" ", addressComponents);
                String encodedFullAddress = "";
                try {
                    encodedFullAddress = URLEncoder.encode(fullAddress, "UTF-8") + ".json";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                RequestParams params = new RequestParams();
                params.put("access_token", API_KEY);

                asyncHttpClient.get(BASE_REQUEST_URL + encodedFullAddress, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.d(TAG, "HTTP request succeeded");
                        JSONObject jsonObject = json.jsonObject;
                        try {
                            JSONArray features = jsonObject.getJSONArray("features");
                            JSONObject mostRelevantFeature = features.getJSONObject(0);
                            JSONArray center = mostRelevantFeature.getJSONArray("center");
                            double longitude = center.getDouble(0);
                            double latitude = center.getDouble(1);
                            Log.d(TAG, "latitude: " + latitude);
                            Log.d(TAG, "longitude: " + longitude);

                            Location location = new Location();
                            ParseGeoPoint geoPoint = new ParseGeoPoint(latitude, longitude);
                            location.setUser(ParseUser.getCurrentUser());
                            location.setName(addressName);
                            location.setLocation(geoPoint);

                            location.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.e(TAG, "Error while saving location", e);
                                        return;
                                    }
                                    Log.i(TAG, "Location save was successful!");
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "HTTP request failed");
                    }
                });

                ProfileFragment profileFragment = new ProfileFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flContainer, profileFragment, "findThisFragment")
                        .addToBackStack(null)
                        .commit();

            }
        });

    }
}