package com.example.habittrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.parse.ParseUser;

public class StartActivity extends AppCompatActivity {

    public static final String TAG = "StartActivity";
    private Button btnStartLogin;
    private Button btnStartRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnStartLogin = findViewById(R.id.btnStartLogin);
        btnStartRegister = findViewById(R.id.btnStartRegister);

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        btnStartLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goLoginActivity();
            }
        });

        btnStartRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goRegisterActivity();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish(); // removes LoginActivity from stack
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish(); // removes LoginActivity from stack
    }

    private void goRegisterActivity() {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
        finish(); // removes LoginActivity from stack
    }

}