package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private RadioGroup rgRole;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize helper tools
        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        // Link visual elements from activity_main.xml
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        // Handle Login button action
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                int selectedId = rgRole.getCheckedRadioButtonId();
                RadioButton rbSelected = findViewById(selectedId);
                String role = rbSelected.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check user data against database helper tables
                boolean isValid = dbHelper.checkUserLogin(username, password, role);

                if (isValid) {
                    // Save info to shared memory session
                    session.createLoginSession(username, role);

                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    // Route user to correct dashboard view (Will create these activities next)
                    if (role.equals("Student")) {
                        Intent intent = new Intent(MainActivity.this, StudentHomeActivity.class);
                        startActivity(intent);
                        finish(); // Closes the login activity so the back button doesn't return here
                    }
                    else {
                        // startActivity(new Intent(MainActivity.this, LecturerHomeActivity.class));
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Invalid credentials or role!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Redirect user to the Account Registration process screen
        // Redirect user to the Account Registration process screen
        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StudentRegisterActivity.class));
            }
        });

    }
}
