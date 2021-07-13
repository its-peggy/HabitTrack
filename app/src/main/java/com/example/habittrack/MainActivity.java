package com.example.habittrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.habittrack.fragments.CreateFragment;
import com.example.habittrack.fragments.HomeFragment;
import com.example.habittrack.fragments.ProgressFragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private Button btnLogout;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // btnLogout = findViewById(R.id.btnLogout);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);

//        btnLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ParseUser.logOut();
//                Intent i = new Intent(MainActivity.this, StartActivity.class);
//                startActivity(i);
//            }
//        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        // Toast.makeText(MainActivity.this, "Home!", Toast.LENGTH_SHORT).show();
                        fragment = new HomeFragment();
                        break;
                    case R.id.action_create:
                        // Toast.makeText(MainActivity.this, "Create!", Toast.LENGTH_SHORT).show();
                        fragment = new CreateFragment();
                        break;
                    case R.id.action_progress:
                    default:
                        // Toast.makeText(MainActivity.this, "Profile!", Toast.LENGTH_SHORT).show();
                        fragment = new ProgressFragment();
                        break;
                }
                // replace frame layout with whichever fragment corresponds to the button selected
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }
}