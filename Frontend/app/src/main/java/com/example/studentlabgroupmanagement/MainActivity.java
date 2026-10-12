package com.example.studentlabgroupmanagement;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);

        // Optional: Automatically check if user is already logged in,
        // otherwise route them to LoginActivity after a brief delay.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (session.isLoggedIn()) {
                String role = session.getUserRole();
                if ("Student".equalsIgnoreCase(role)) {
                    startActivity(new Intent(MainActivity.this, StudentHomeActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, LecturerHomeActivity.class));
                }
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            finish();
        }, 1500); // 1.5-second splash delay
    }
}