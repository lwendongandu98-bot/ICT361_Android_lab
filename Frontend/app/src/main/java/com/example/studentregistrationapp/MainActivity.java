package com.example.studentregistrationapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.studentregistrationapp.data.model.Student;
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
        EditText nameInput = findViewById(R.id.studentNameInput);
        Button registerButton = findViewById(R.id.registerButton);

        // 3. Trigger ViewModel logic on button click
        if (registerButton != null && nameInput != null) {
            registerButton.setOnClickListener(v -> {
                String inputName = nameInput.getText().toString();

                if (!inputName.isEmpty()) {
                    Student newStudent = new Student();
                    newStudent.setName(inputName);

                    studentViewModel.registerStudent(newStudent);
                    Toast.makeText(MainActivity.this, "Student registered: " + inputName, Toast.LENGTH_SHORT).show();
                    nameInput.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}