package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etRecoverUsername, etRecoverEmail;
    private Button btnRecoverSubmit;
    private TextView tvBackToLoginFromRecover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etRecoverUsername = findViewById(R.id.etRecoverUsername);
        etRecoverEmail = findViewById(R.id.etRecoverEmail);
        btnRecoverSubmit = findViewById(R.id.btnRecoverSubmit);
        tvBackToLoginFromRecover = findViewById(R.id.tvBackToLoginFromRecover);

        btnRecoverSubmit.setOnClickListener(v -> {
            String username = etRecoverUsername.getText().toString().trim();
            String email = etRecoverEmail.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter all verification details", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                StudentEntity student = AppDatabase.getInstance(getApplicationContext())
                        .studentDao()
                        .getStudentById(username);

                String foundPassword = null;
                if (student != null && email.equalsIgnoreCase(student.getEmail())) {
                    foundPassword = student.getPassword();
                }

                final String finalPassword = foundPassword;
                runOnUiThread(() -> {
                    if (finalPassword != null) {
                        new AlertDialog.Builder(ForgotPasswordActivity.this)
                                .setTitle("Account Password Found")
                                .setMessage("Your account password credential is:\n\n" + finalPassword)
                                .setPositiveButton("OK", (dialog, which) -> finish())
                                .show();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "No matching account registration details found!", Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        });

        tvBackToLoginFromRecover.setOnClickListener(v -> finish());
    }
}