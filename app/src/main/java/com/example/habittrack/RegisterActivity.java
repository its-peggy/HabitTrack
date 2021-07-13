package com.example.habittrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "RegisterActivity";
    private EditText etRegisterFirstName;
    private EditText etRegisterLastName;
    private EditText etRegisterUsername;
    private EditText etRegisterPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegisterFirstName = findViewById(R.id.etRegisterFirstName);
        etRegisterLastName = findViewById(R.id.etRegisterLastName);
        etRegisterUsername = findViewById(R.id.etRegisterUsername);
        etRegisterFirstName = findViewById(R.id.etRegisterFirstName);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick register button");
                String firstName = etRegisterFirstName.getText().toString();
                String lastName = etRegisterLastName.getText().toString();
                String username = etRegisterUsername.getText().toString();
                String password = etRegisterPassword.getText().toString();
                registerUser(username, password, firstName, lastName);
            }
        });
    }

    private void registerUser(String username, String password, String firstName, String lastName) {
        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set properties
        user.setUsername(username);
        user.setPassword(password);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with signup", e);
                    Toast.makeText(RegisterActivity.this, "Issue with signup!", Toast.LENGTH_SHORT).show();
                    return;
                }
                goMainActivity();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
