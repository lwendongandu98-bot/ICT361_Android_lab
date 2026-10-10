package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etRecoverUsername, etRecoverEmail;
    private Button btnRecoverSubmit;
    private TextView tvBackToLoginFromRecover;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password); // Links the interface view layout code

        dbHelper = new DatabaseHelper(this);

        // Bind layout IDs
        etRecoverUsername = findViewById(R.id.etRecoverUsername);
        etRecoverEmail = findViewById(R.id.etRecoverEmail);
        btnRecoverSubmit = findViewById(R.id.btnRecoverSubmit);
        tvBackToLoginFromRecover = findViewById(R.id.tvBackToLoginFromRecover);

        btnRecoverSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etRecoverUsername.getText().toString().trim();
                String email = etRecoverEmail.getText().toString().trim();

                if (username.isEmpty() || email.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter all verification details", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Query match execution lookups
                String foundPassword = dbHelper.getRecoveredPassword(username, email);

                if (foundPassword != null) {
                    // Display security alert credentials verification dialog match lookup result
                    new AlertDialog.Builder(ForgotPasswordActivity.this)
                            .setTitle("Account Password Found")
                            .setMessage("Your account password credential is:\n\n" + foundPassword)
                            .setPositiveButton("OK", (dialog, which) -> finish()) // Finish and go back to login automatically
                            .show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "No matching account registration details found!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Close page navigation reference hook
        tvBackToLoginFromRecover.setOnClickListener(v -> finish());
    }
}
