package com.example.studentregistrationapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.studentregistrationapp.viewmodel.StudentViewModel;

public class MainActivity extends AppCompatActivity {

    private StudentViewModel studentViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize ViewModel tied to MainActivity's lifecycle
        studentViewModel = new ViewModelProvider(this).get(StudentViewModel.class);

        // 2. Locate UI elements from activity_main.xml
        EditText nameInput = findViewById(R.id.studentNameInput); // Ensure ID matches XML
        Button registerButton = findViewById(R.id.registerButton); // Ensure ID matches XML

        // 3. Observe LiveData from ViewModel (automatically updates UI when state changes)
        studentViewModel.getRegistrationStatus().observe(this, status -> {
            if (status != null && !status.isEmpty()) {
                Toast.makeText(MainActivity.this, status, Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Trigger ViewModel logic on button click
        if (registerButton != null && nameInput != null) {
            registerButton.setOnClickListener(v -> {
                String inputName = nameInput.getText().toString();
                studentViewModel.registerStudent(inputName);
            });
        }
    }
}
