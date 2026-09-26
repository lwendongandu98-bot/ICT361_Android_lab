package com.example.studentlabgroupmanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class StudentRegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegName, etRegStudentNum, etRegProgram, etRegPassword;
    private Button btnRegisterSubmit;
    private TextView tvBackToLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        // Initialize our local SQLite database tool
        dbHelper = new DatabaseHelper(this);

        // Map visual XML elements from layout to Java objects
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegName = findViewById(R.id.etRegName);
        etRegStudentNum = findViewById(R.id.etRegStudentNum);
        etRegProgram = findViewById(R.id.etRegProgram);
        etRegPassword = findViewById(R.id.etRegPassword);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Capture user input and process account creation
        btnRegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etRegUsername.getText().toString().trim();
                String name = etRegName.getText().toString().trim();
                String studentNum = etRegStudentNum.getText().toString().trim();
                String program = etRegProgram.getText().toString().trim();
                String password = etRegPassword.getText().toString().trim();

                // 1. Data Validation: Ensure no text input fields are blank
                if (username.isEmpty() || name.isEmpty() || studentNum.isEmpty() || program.isEmpty() || password.isEmpty()) {
                    Toast.makeText(StudentRegisterActivity.this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Data Validation: Ensure password matches baseline character security length rules
                if (password.length() < 4) {
                    Toast.makeText(StudentRegisterActivity.this, "Password must be at least 4 characters long", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. Attempt local database insertion (Default assigned lab group is "None")
                boolean success = dbHelper.insertStudent(username, name, studentNum, password, program, "None");

                if (success) {
                    Toast.makeText(StudentRegisterActivity.this, "Account Created Successfully!", Toast.LENGTH_LONG).show();
                    finish(); // Drops the user right back to the Login window automatically
                } else {
                    // Triggers if username primary key conflict occurs in SQLite
                    Toast.makeText(StudentRegisterActivity.this, "Username already exists! Try a different one.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Wires up the updated back text navigation link to close this window immediately
        tvBackToLogin.setOnClickListener(v -> finish());
    }
}
