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
    private TextView tvForgotPassword;

    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        session = new SessionManager(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // LOGIN BUTTON
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                int selectedId = rgRole.getCheckedRadioButtonId();

                if (selectedId == -1) {

                    Toast.makeText(
                            MainActivity.this,
                            "Please select Student or Lecturer",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                RadioButton rbSelected = findViewById(selectedId);
                String role = rbSelected.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {

                    Toast.makeText(
                            MainActivity.this,
                            "Please fill all fields",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                boolean isValid = dbHelper.checkUserLogin(
                        username,
                        password,
                        role
                );

                if (isValid) {

                    session.createLoginSession(
                            username,
                            role
                    );

                    Toast.makeText(
                            MainActivity.this,
                            "Login Successful!",
                            Toast.LENGTH_SHORT
                    ).show();

                    // STUDENT LOGIN
                    if (role.equalsIgnoreCase("Student")) {

                        Intent intent = new Intent(
                                MainActivity.this,
                                StudentHomeActivity.class
                        );

                        startActivity(intent);
                        finish();

                    }
                    // LECTURER LOGIN
                    else if (role.equalsIgnoreCase("Lecturer")) {

                        Intent intent = new Intent(
                                MainActivity.this,
                                LecturerHomeActivity.class
                        );

                        startActivity(intent);
                        finish();
                    }

                } else {

                    Toast.makeText(
                            MainActivity.this,
                            "Invalid credentials!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

        // FORGOT PASSWORD
        tvForgotPassword.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        ForgotPasswordActivity.class
                                )
                        );
                    }
                }
        );

        // REGISTER LINK
        tvRegisterLink.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int selectedId = rgRole.getCheckedRadioButtonId();

                        if (selectedId == -1) {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Please select Student or Lecturer",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        RadioButton rbSelected =
                                findViewById(selectedId);

                        String role =
                                rbSelected.getText().toString();

                        if (role.equalsIgnoreCase("Student")) {

                            startActivity(
                                    new Intent(
                                            MainActivity.this,
                                            StudentRegisterActivity.class
                                    )
                            );

                        } else if (role.equalsIgnoreCase("Lecturer")) {

                            startActivity(
                                    new Intent(
                                            MainActivity.this,
                                            LecturerRegisterActivity.class
                                    )
                            );
                        }
                    }
                }
        );
    }
}